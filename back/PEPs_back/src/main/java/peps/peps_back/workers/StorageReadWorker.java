package peps.peps_back.workers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;
import peps.peps_back.items.Sound;
import peps.peps_back.repositories.SoundRepository;
import peps.peps_back.services.MinioStorageService;

import javax.annotation.PostConstruct;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * First link in the Download Chain (Pipeline 2 – Request from ESP32 / HTTP client).
 *
 * <p>Subscribes to {@code retrieval-request-stream}. For every incoming download request:
 * <ol>
 *   <li>Looks up the {@link Sound} row in PostgreSQL to obtain its MinIO object key
 *       ({@code chemin}).</li>
 *   <li>Downloads the raw audio bytes from MinIO via {@link MinioStorageService}.</li>
 *   <li>Forwards a new message to {@code device-delivery-stream} containing the
 *       Base-64 encoded audio and correlation metadata so that {@link DevicePrepWorker}
 *       can transcode and cache the result.</li>
 * </ol>
 *
 * <p>Responsibility boundary: this worker is only in charge of <em>fetching</em> audio
 * from storage. All format adaptation (sample-rate conversion, mono mixing) and Redis
 * result caching are delegated to {@link DevicePrepWorker}.
 *
 * @author Santiago Alexander RODRIGUEZ TRIANA
 */
@Component
@Profile("audio-worker")
public class StorageReadWorker implements StreamListener<String, MapRecord<String, String, String>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageReadWorker.class);

    @Autowired
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private MinioStorageService minioStorageService;

    @Autowired
    private SoundRepository soundRepository;

    /** Redis Stream key this worker consumes. */
    private final String streamKey = "retrieval-request-stream";

    /** Consumer group name — ensures each download request is handled once. */
    private final String consumerGroup = "storage-readers";

    /**
     * Registers this worker with the Redis Stream listener container.
     * Creates the consumer group if it does not already exist.
     */
    @PostConstruct
    public void init() {
        try {
            redisTemplate.opsForStream().createGroup(streamKey, consumerGroup);
        } catch (Exception e) {
            // Group already exists — safe to ignore.
        }
        container.receive(
                Consumer.from(consumerGroup, "worker-1"),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                this);
    }

    /**
     * Triggered when a download request arrives on {@code retrieval-request-stream}.
     *
     * <p>Expected message fields:
     * <ul>
     *   <li>{@code soundId} – Integer primary key of the {@code Sound} row to retrieve.</li>
     *   <li>{@code jobId}   – UUID correlation identifier used to store the final result
     *                          in Redis under {@code ready-audio:{jobId}}.</li>
     * </ul>
     *
     * @param message the Redis Stream record
     */
    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        LOGGER.info("StorageReadWorker received download request: {}", message.getId());
        Map<String, String> fields = message.getValue();

        String jobId = fields.get("jobId");
        String soundIdStr = fields.get("soundId");

        try {
            // --- 1. Validate required fields ---
            if (jobId == null || soundIdStr == null) {
                LOGGER.error("StorageReadWorker: missing soundId or jobId in message {}. Skipping.",
                        message.getId());
                return;
            }

            int soundId = Integer.parseInt(soundIdStr);

            // --- 2. Look up the Sound entity in PostgreSQL to get the MinIO object key ---
            Optional<Sound> optSound = soundRepository.findById(soundId);
            if (optSound.isEmpty()) {
                LOGGER.error("StorageReadWorker: Sound ID {} not found in DB. Writing error status.", soundId);
                redisTemplate.opsForValue().set("ready-audio:" + jobId, "ERROR:SOUND_NOT_FOUND",
                        60, TimeUnit.SECONDS);
                return;
            }

            Sound sound = optSound.get();
            String objectKey = sound.getChemin();

            if (objectKey == null || objectKey.isBlank()) {
                LOGGER.warn("StorageReadWorker: Sound {} has no MinIO object key (chemin is empty).", soundId);
                redisTemplate.opsForValue().set("ready-audio:" + jobId, "ERROR:NOT_UPLOADED_YET",
                        60, TimeUnit.SECONDS);
                return;
            }

            // --- 3. Download raw audio bytes from MinIO ---
            byte[] audioBytes = minioStorageService.downloadSound(objectKey);
            LOGGER.info("StorageReadWorker: downloaded {} bytes for Sound {} from MinIO.", audioBytes.length, soundId);

            // --- 4. Encode bytes to Base-64 for transport through the Redis Stream ---
            String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);

            // --- 5. Forward the payload to DevicePrepWorker via device-delivery-stream.
            //        DevicePrepWorker is responsible for transcoding and Redis caching. ---
            Map<String, String> deliveryPayload = new HashMap<>();
            deliveryPayload.put("jobId", jobId);
            deliveryPayload.put("soundId", soundIdStr);
            deliveryPayload.put("audioBase64", audioBase64);
            deliveryPayload.put("extension", sound.getExtension() != null ? sound.getExtension() : "");

            redisTemplate.opsForStream().add("device-delivery-stream", deliveryPayload);
            LOGGER.info("StorageReadWorker: forwarded Sound {} to device-delivery-stream for jobId '{}'.",
                    soundId, jobId);

        } catch (Exception e) {
            LOGGER.error("StorageReadWorker: failed for message {}: {}", message.getId(), e.getMessage(), e);
            // Write an error marker so the HTTP polling endpoint does not hang.
            if (jobId != null) {
                redisTemplate.opsForValue().set("ready-audio:" + jobId, "ERROR:" + e.getMessage(),
                        60, TimeUnit.SECONDS);
            }
        } finally {
            // Always ACK — avoid infinite retry loops on permanent failures.
            redisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, message.getId());
        }
    }
}
