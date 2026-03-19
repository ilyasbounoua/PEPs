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
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Second link in the Download Chain (Pipeline 2 – From MinIO to ESP32).
 *
 * <p>Subscribes to {@code device-delivery-stream}. For every incoming message this
 * worker:
 * <ol>
 *   <li>Decodes the Base-64 audio bytes forwarded by {@link StorageReadWorker}.</li>
 *   <li>Attempts to <em>transcode</em> the audio to a format the ESP32 DAC can
 *       play natively: <strong>16 000 Hz, Mono, 16-bit signed PCM (WAV)</strong>.
 *       Transcoding uses only the JDK's built-in {@link javax.sound.sampled} API,
 *       so it is guaranteed to work for WAV inputs.  For MP3 or other container
 *       formats that the JDK cannot decode natively, the raw bytes are passed
 *       through unchanged — the ESP32 AudioI2S library can decode MP3 directly.</li>
 *   <li>Base-64 encodes the final bytes and writes them into Redis under
 *       {@code ready-audio:{jobId}} with a 60-second TTL so the HTTP polling
 *       endpoint can return them to the client.</li>
 * </ol>
 *
 * @author Santiago Alexander RODRIGUEZ TRIANA
 */
@Component
@Profile("audio-worker")
public class DevicePrepWorker implements StreamListener<String, MapRecord<String, String, String>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevicePrepWorker.class);

    /**
     * Target sample rate for ESP32 DAC output.
     * The ESP32-audioI2S library defaults to 16 kHz for reliable playback without
     * overrunning the I2S buffer.
     */
    private static final float TARGET_SAMPLE_RATE = 16_000f;

    /**
     * Target number of audio channels (1 = Mono).
     * Mono reduces the payload size by half and avoids stereo-to-mono mixing issues
     * at the DAC level.
     */
    private static final int TARGET_CHANNELS = 1;

    /**
     * Target bit depth (16-bit signed PCM).
     * This matches the ESP32 I2S peripheral's native resolution.
     */
    private static final int TARGET_SAMPLE_SIZE_BITS = 16;

    /**
     * TTL in seconds for the Redis result key.
     * 60 seconds is long enough for the HTTP client to poll {@code /audio/download/status/{jobId}}.
     */
    private static final long RESULT_TTL_SECONDS = 60L;

    @Autowired
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /** Redis Stream key this worker consumes. */
    private final String streamKey = "device-delivery-stream";

    /** Consumer group name — ensures each delivery job is processed once. */
    private final String consumerGroup = "device-formatters";

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
     * Triggered when a processed-audio delivery message arrives on
     * {@code device-delivery-stream} (sent by {@link StorageReadWorker}).
     *
     * <p>Expected message fields:
     * <ul>
     *   <li>{@code jobId}      – UUID correlation identifier.</li>
     *   <li>{@code soundId}    – Integer primary key of the {@code Sound} row (for logging).</li>
     *   <li>{@code audioBase64} – Base-64 encoded raw audio bytes from MinIO.</li>
     *   <li>{@code extension}  – File extension (e.g. {@code mp3}, {@code wav}).
     *                             Used to decide the transcoding strategy.</li>
     * </ul>
     *
     * @param message the Redis Stream record
     */
    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        LOGGER.info("DevicePrepWorker received delivery job: {}", message.getId());
        Map<String, String> fields = message.getValue();

        String jobId = fields.get("jobId");
        String soundIdStr = fields.get("soundId");
        String audioBase64 = fields.get("audioBase64");
        String extension = fields.getOrDefault("extension", "").toLowerCase().replace(".", "");

        try {
            // --- 1. Validate required fields ---
            if (jobId == null || audioBase64 == null) {
                LOGGER.error("DevicePrepWorker: missing jobId or audioBase64 in message {}. Skipping.",
                        message.getId());
                return;
            }

            // --- 2. Decode the Base-64 audio payload ---
            byte[] inputBytes = Base64.getDecoder().decode(audioBase64);
            LOGGER.info("DevicePrepWorker: decoded {} bytes for Sound {} (ext='{}').",
                    inputBytes.length, soundIdStr, extension);

            // --- 3. Transcode to ESP32-compatible PCM WAV (16 kHz, Mono, 16-bit) ---
            // javax.sound.sampled can only decode PCM-based WAV / AIFF natively.
            // For MP3 (or any unsupported container), we fall back to passing the bytes
            // through unchanged — the ESP32 AudioI2S library handles MP3 decoding itself.
            byte[] outputBytes = transcodeForEsp32(inputBytes, extension);

            // --- 4. Encode the final bytes back to Base-64 ---
            String resultBase64 = Base64.getEncoder().encodeToString(outputBytes);

            // --- 5. Store in Redis with a TTL so the HTTP polling endpoint can retrieve it ---
            // Key pattern: "ready-audio:{jobId}"
            redisTemplate.opsForValue().set("ready-audio:" + jobId, resultBase64,
                    RESULT_TTL_SECONDS, TimeUnit.SECONDS);

            LOGGER.info("DevicePrepWorker: stored {} bytes (Base-64) in Redis key 'ready-audio:{}' (TTL {}s).",
                    outputBytes.length, jobId, RESULT_TTL_SECONDS);

        } catch (Exception e) {
            LOGGER.error("DevicePrepWorker: failed to process message {}: {}", message.getId(), e.getMessage(), e);
            // Write an error marker so the polling endpoint does not hang indefinitely.
            if (jobId != null) {
                redisTemplate.opsForValue().set("ready-audio:" + jobId,
                        "ERROR:" + e.getMessage(), RESULT_TTL_SECONDS, TimeUnit.SECONDS);
            }
        } finally {
            // Always ACK — avoid infinite retry on permanent failures.
            redisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, message.getId());
        }
    }

    // -------------------------------------------------------------------------
    // Transcoding helpers
    // -------------------------------------------------------------------------

    /**
     * Attempts to transcode raw audio bytes to a PCM WAV format suitable for the
     * ESP32 DAC: {@value #TARGET_SAMPLE_RATE} Hz, {@value #TARGET_CHANNELS} channel(s),
     * {@value #TARGET_SAMPLE_SIZE_BITS}-bit signed little-endian PCM.
     *
     * <p>The JDK's {@link AudioSystem} natively handles PCM WAV and AIFF containers.
     * If the input is MP3 or any format that {@code AudioSystem} cannot decode
     * (it throws an {@link Exception} during stream opening), this method falls back
     * to returning the original bytes so the ESP32's native MP3 decoder can handle
     * them directly.
     *
     * @param inputBytes raw audio bytes (WAV, MP3, or other)
     * @param extension  file extension hint (e.g. {@code "wav"}, {@code "mp3"})
     * @return PCM WAV bytes if transcoding succeeded; original bytes otherwise
     */
    private byte[] transcodeForEsp32(byte[] inputBytes, String extension) {
        // MP3: javax.sound.sampled cannot decode it without a third-party SPI plugin.
        // Pass through — the ESP32 AudioI2S library decodes MP3 natively.
        if ("mp3".equals(extension)) {
            LOGGER.info("DevicePrepWorker: MP3 detected — passing bytes through for ESP32 native decoding.");
            return inputBytes;
        }

        try {
            // Open the input as an AudioInputStream so we can inspect its format.
            ByteArrayInputStream bais = new ByteArrayInputStream(inputBytes);
            try (AudioInputStream sourceStream = AudioSystem.getAudioInputStream(bais)) {
                AudioFormat sourceFormat = sourceStream.getFormat();
                LOGGER.info("DevicePrepWorker: source audio format — {}", sourceFormat);

                // Build the target format: 16 kHz, Mono, 16-bit signed PCM, little-endian.
                AudioFormat targetFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        TARGET_SAMPLE_RATE,
                        TARGET_SAMPLE_SIZE_BITS,
                        TARGET_CHANNELS,
                        /* frameSize (bytes) = channels * (sampleBits / 8) */
                        TARGET_CHANNELS * (TARGET_SAMPLE_SIZE_BITS / 8),
                        TARGET_SAMPLE_RATE,
                        /* bigEndian */ false);

                // Check whether the JDK knows how to convert this source format.
                if (!AudioSystem.isConversionSupported(targetFormat, sourceFormat)) {
                    LOGGER.warn("DevicePrepWorker: conversion from {} to {} is not supported by AudioSystem."
                            + " Passing bytes through unchanged.", sourceFormat, targetFormat);
                    return inputBytes;
                }

                // Perform the sample-rate / channel conversion.
                try (AudioInputStream convertedStream = AudioSystem.getAudioInputStream(targetFormat, sourceStream);
                     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                    // Write the converted PCM samples as a proper WAV file (with RIFF header).
                    AudioSystem.write(convertedStream, javax.sound.sampled.AudioFileFormat.Type.WAVE, baos);
                    byte[] result = baos.toByteArray();
                    LOGGER.info("DevicePrepWorker: transcoded to PCM WAV — {} Hz, {} ch, 16-bit ({} bytes).",
                            (int) TARGET_SAMPLE_RATE, TARGET_CHANNELS, result.length);
                    return result;
                }
            }
        } catch (Exception e) {
            // Transcoding failed (e.g. unsupported format, malformed WAV header).
            // Fall back to the original bytes so the pipeline does not break.
            LOGGER.warn("DevicePrepWorker: transcoding failed ({}). Passing original bytes through.",
                    e.getMessage());
            return inputBytes;
        }
    }
}
