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

import javax.annotation.PostConstruct;
import java.util.Base64;

/**
 * First link in the Assembly Line (Pipeline 1 – Audio Upload).
 *
 * <p>
 * Subscribes to {@code upload-audio-stream}. For every incoming job this
 * worker:
 * <ol>
 * <li>Decodes the Base-64 audio payload.</li>
 * <li>Validates the audio format by inspecting the binary <em>magic bytes</em>
 * (supports MP3 and WAV without any external library).</li>
 * <li>Forwards valid messages to {@code storage-upload-stream} so that
 * {@link StorageWorker} can persist them in MinIO.</li>
 * <li>Drops invalid messages with an ACK so Redis does not retry them
 * indefinitely.</li>
 * </ol>
 *
 * @author Santiago Alexander RODRIGUEZ TRIANA
 */
@Component
@Profile("audio-worker") // Only instantiated if the server has the "audio-worker" role
public class RawAudioWorker implements StreamListener<String, MapRecord<String, String, String>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawAudioWorker.class);

    @Autowired
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /** Redis Stream key this worker consumes. */
    private final String streamKey = "upload-audio-stream";

    /**
     * Consumer group name — ensures each job is handled by exactly one worker
     * instance.
     */
    private final String consumerGroup = "audio-processors";

    /**
     * Executed automatically at startup. Creates the consumer group (silently
     * ignores the exception if it already exists) and registers this worker with
     * the listener container to start receiving messages.
     */
    @PostConstruct
    public void init() {
        try {
            redisTemplate.opsForStream().createGroup(streamKey, consumerGroup);
        } catch (Exception e) {
            // Group already exists — safe to ignore.
        }

        // ReadOffset.lastConsumed() ensures only unread messages are delivered.
        // The worker-1 it can be change for a global variable in case of scalability
        // with other containers
        container.receive(
                Consumer.from(consumerGroup, "worker-1"),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                this);
    }

    /**
     * Entry point triggered for every incoming message on
     * {@code upload-audio-stream}.
     *
     * <p>
     * Expected message fields:
     * <ul>
     * <li>{@code soundId} – Integer primary key of the {@code Sound} row.</li>
     * <li>{@code audioBase64} – Base-64 encoded raw audio bytes.</li>
     * <li>{@code contentType} – MIME type string (e.g. {@code audio/mpeg}).</li>
     * </ul>
     *
     * @param message the Redis Stream record
     */
    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        String soundName = message.getValue().getOrDefault("soundName", "unknown");
        LOGGER.info("RawAudioWorker received message: {} for sound '{}'", message.getId(), soundName);

        String audioBase64 = message.getValue().get("audioBase64");

        // --- 1. Null / empty guard ---
        if (audioBase64 == null || audioBase64.isBlank()) {
            LOGGER.error("RawAudioWorker: missing audioBase64 in message {} for sound '{}'. Skipping.", 
                         message.getId(), soundName);
            ack(message);
            return;
        }

        // --- 2. Decode the Base-64 payload ---
        byte[] audioBytes;
        try {
            audioBytes = Base64.getDecoder().decode(audioBase64);
        } catch (IllegalArgumentException e) {
            LOGGER.error("RawAudioWorker: audioBase64 for sound '{}' (message {}) is not valid Base-64. Skipping.",
                    soundName, message.getId());
            ack(message);
            return;
        }

        // --- 3. Validate audio format using magic bytes ---
        if (!isValidAudioFormat(audioBytes)) {
            LOGGER.error("RawAudioWorker: unsupported or corrupt format in sound '{}' (message {}). "
                    + "Supported: MP3, WAV, OGG, M4A. Skipping.", soundName, message.getId());
            ack(message);
            return;
        }

        LOGGER.info("RawAudioWorker: format OK for sound '{}' (id: {}) — detected {} ({} bytes). Forwarding to storage.",
                soundName, message.getId(), detectFormatName(audioBytes), audioBytes.length);

        // --- 4. Forward the validated payload to the next stage
        redisTemplate.opsForStream().add("storage-upload-stream", message.getValue());
        LOGGER.info("RawAudioWorker: forwarded sound '{}' to storage-upload-stream.", soundName);

        // --- 5. ACK ---
        ack(message);
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    /**
     * Acknowledges a stream message, marking it as processed for this consumer
     * group so it is not delivered again.
     *
     * @param message the record to acknowledge
     */
    private void ack(MapRecord<String, String, String> message) {
        redisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, message.getId());
    }

    /**
     * Validates an audio file by inspecting its <em>magic bytes</em> (file
     * signature). No external libraries are required.
     *
     * <p>
     * Supported signatures:
     * <table border="1">
     * <tr>
     * <th>Format</th>
     * <th>Offset 0</th>
     * <th>Offset 1</th>
     * <th>Offset 2</th>
     * <th>Offset 3</th>
     * </tr>
     * <tr>
     * <td>WAV</td>
     * <td>0x52 ('R')</td>
     * <td>0x49 ('I')</td>
     * <td>0x46 ('F')</td>
     * <td>0x46 ('F')</td>
     * </tr>
     * <tr>
     * <td>MP3 ID3</td>
     * <td>0x49 ('I')</td>
     * <td>0x44 ('D')</td>
     * <td>0x33 ('3')</td>
     * <td>—</td>
     * </tr>
     * <tr>
     * <td>MP3 sync</td>
     * <td>0xFF</td>
     * <td>0xFB | 0xF3 | 0xF2</td>
     * <td>—</td>
     * <td>—</td>
     * </tr>
     * </table>
     *
     * @param bytes the raw audio bytes to inspect
     * @return {@code true} if the bytes match a known supported container format
     */
    private boolean isValidAudioFormat(byte[] bytes) {
        if (bytes == null || bytes.length < 12) {
            return false;
        }

        // WAV — RIFF container: "RIFF" (52 49 46 46)
        if (bytes[0] == 0x52 && bytes[1] == 0x49 && bytes[2] == 0x46 && bytes[3] == 0x46) {
            return true;
        }

        // OGG — "OggS" (4F 67 67 53)
        if (bytes[0] == 0x4F && bytes[1] == 0x67 && bytes[2] == 0x67 && bytes[3] == 0x53) {
            return true;
        }

        // MP3 with ID3 metadata tag: "ID3" (49 44 33)
        if (bytes[0] == 0x49 && bytes[1] == 0x44 && bytes[2] == 0x33) {
            return true;
        }

        // MP3 bare MPEG sync word (no ID3 tag): 0xFF followed by 0xFB / 0xF3 / 0xF2
        if ((bytes[0] & 0xFF) == 0xFF) {
            int second = bytes[1] & 0xFF;
            if (second == 0xFB || second == 0xF3 || second == 0xF2) {
                return true;
            }
        }

        // M4A / MP4 — Look for "ftyp" at offset 4
        if (bytes[4] == 0x66 && bytes[5] == 0x74 && bytes[6] == 0x79 && bytes[7] == 0x70) {
            return true;
        }

        return false;
    }

    /**
     * Returns a short human-readable label for the detected audio format.
     * Used only for diagnostic log messages.
     *
     * @param bytes the raw audio bytes (must not be null)
     * @return {@code "WAV"}, {@code "MP3 (ID3)"}, {@code "MP3 (sync)"} or
     *         {@code "unknown"}
     */
    private String detectFormatName(byte[] bytes) {
        if (bytes == null || bytes.length < 8) {
            return "unknown";
        }
        // WAV: "RI" prefix
        if (bytes[0] == 0x52 && bytes[1] == 0x49) {
            return "WAV";
        }
        // OGG
        if (bytes[0] == 0x4F && bytes[1] == 0x67) {
            return "OGG";
        }
        // MP3 with ID3: "ID" prefix
        if (bytes[0] == 0x49 && bytes[1] == 0x44) {
            return "MP3 (ID3)";
        }
        // MP3 bare sync
        if ((bytes[0] & 0xFF) == 0xFF) {
            return "MP3 (sync)";
        }
        // M4A
        if (bytes[4] == 0x66 && bytes[5] == 0x74) {
            return "M4A/MP4";
        }
        return "unknown";
    }
}
