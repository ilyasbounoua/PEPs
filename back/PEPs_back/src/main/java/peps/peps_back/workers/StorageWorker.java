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
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * Second link in the Assembly Line (Pipeline 1: Upload Audio to MinIO).
 *
 * <p>
 * Receives a message from the {@link RawAudioWorker} on
 * {@code storage-upload-stream}. The message payload contains:
 * <ul>
 * <li>{@code soundId} – primary key of the {@link Sound} row to update</li>
 * <li>{@code audioBase64} – Base-64 encoded raw audio bytes</li>
 * <li>{@code contentType} – MIME type (e.g. {@code audio/mpeg})</li>
 * </ul>
 *
 * <p>
 * This worker:
 * <ol>
 * <li>Decodes the Base-64 audio payload.</li>
 * <li>Uploads it to MinIO via {@link MinioStorageService}.</li>
 * <li>Updates {@code Sound.chemin} in PostgreSQL with the generated object
 * key.</li>
 * <li>Acknowledges the Redis message.</li>
 * </ol>
 *
 * @author Santiago Alexander RODRIGUEZ TRIANA
 */
@Component
@Profile("audio-worker")
public class StorageWorker implements StreamListener<String, MapRecord<String, String, String>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageWorker.class);

    @Autowired
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private MinioStorageService minioStorageService;

    @Autowired
    private SoundRepository soundRepository;

    private final String streamKey = "storage-upload-stream";
    private final String consumerGroup = "storage-handlers";

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
     * Triggered whenever a processed-audio message arrives on
     * {@code storage-upload-stream}.
     *
     * <p>
     * Expected message fields:
     * <ul>
     * <li>{@code soundId} – Integer ID of the Sound row</li>
     * <li>{@code audioBase64} – Base-64 encoded audio bytes</li>
     * <li>{@code contentType} – MIME type string</li>
     * </ul>
     *
     * @param message the Redis Stream record
     */
    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        LOGGER.info("StorageWorker received job: {}", message.getId());
        Map<String, String> fields = message.getValue();

        try {
            // --- 1. Parse the payload fields ---
            String soundIdStr = fields.get("soundId");
            String audioBase64 = fields.get("audioBase64");
            String contentType = fields.getOrDefault("contentType", "audio/mpeg");

            if (soundIdStr == null || audioBase64 == null) {
                LOGGER.error("StorageWorker: missing required fields in message {}. Skipping.", message.getId());
                redisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, message.getId());
                return;
            }

            int soundId = Integer.parseInt(soundIdStr);

            // --- 2. Look up the Sound entity in the DB ---
            Optional<Sound> optSound = soundRepository.findById(soundId);
            if (optSound.isEmpty()) {
                LOGGER.error("StorageWorker: Sound ID {} not found in DB. Skipping.", soundId);
                redisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, message.getId());
                return;
            }
            Sound sound = optSound.get();

            // --- 3. Decode the Base-64 audio bytes ---
            byte[] audioBytes = Base64.getDecoder().decode(audioBase64);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(audioBytes);

            // --- 4. Upload to MinIO ---
            String objectKey = minioStorageService.uploadSound(
                    sound.getNom(),
                    sound.getTypeSon(),
                    sound.getExtension(),
                    contentType,
                    inputStream,
                    audioBytes.length);

            LOGGER.info("StorageWorker: uploaded '{}' to MinIO as '{}'", sound.getNom(), objectKey);

            // --- 5. Persist the MinIO object key into Sound.chemin ---
            sound.setChemin(objectKey);
            soundRepository.save(sound);
            LOGGER.info("StorageWorker: updated Sound {} chemin = '{}'", soundId, objectKey);

        } catch (Exception e) {
            LOGGER.error("StorageWorker: failed to process message {}: {}", message.getId(), e.getMessage(), e);
        } finally {
            // ACK always — avoid infinite retry loops on permanent failures.
            redisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, message.getId());
        }
    }
}
// It's possible to opotmize giving directly the decoded audio bytes from the
// RawAudioWorker to the StorageWorker
// But for this case is enough to send the base64 string