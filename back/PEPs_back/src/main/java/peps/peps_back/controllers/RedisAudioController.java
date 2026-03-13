package peps.peps_back.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import peps.peps_back.items.Sound;
import peps.peps_back.repositories.SoundRepository;
import peps.peps_back.services.AudioJobPublisher;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller for interacting with the Audio Infrastructure.
 *
 * <p>
 * Only active in the {@code audio-worker} Spring profile thanks to the
 * {@code @Profile} annotation. This ensures resource isolation — Nginx routes
 * {@code /api/audio/*} traffic exclusively to the {@code backend-audio}
 * replica.
 *
 * <h2>Upload flow (Pipeline 1)</h2>
 * 
 * <pre>
 * POST /audio/upload?soundId={id}  (multipart file)
 *   → queues job on Redis Stream "upload-audio-stream"
 *   → RawAudioWorker → StorageWorker (MinIO upload + DB update)
 * </pre>
 *
 * <h2>Download flow (Pipeline 2)</h2>
 * 
 * <pre>
 * POST /audio/download/{soundId}
 *   → queues job on "retrieval-request-stream"
 *   → StorageReadWorker (MinIO download → stored in Redis)
 *
 * GET /audio/download/status/{jobId}
 *   → polls Redis for the ready audio bytes
 * </pre>
 *
 * @author Santiago Alexander RODRIGUEZ TRIANA
 */
@RestController
@RequestMapping("/audio")
@Profile("audio-worker")
public class RedisAudioController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisAudioController.class);

    @Autowired
    private AudioJobPublisher audioJobPublisher;

    @Autowired
    private SoundRepository soundRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // -------------------------------------------------------------------------
    // POST /audio/upload?soundId={id}
    // -------------------------------------------------------------------------

    /**
     * Accepts a multipart audio file and a Sound ID, encodes the bytes as
     * Base-64, and queues an upload job on the Redis Stream.
     *
     * <p>
     * Returns {@code 202 Accepted} immediately — the heavy lifting (validation,
     * MinIO upload, DB update) is handled asynchronously by the workers.
     *
     * @param soundId the ID of the existing Sound row whose {@code chemin} will be
     *                updated
     * @param file    the audio file to upload (multipart/form-data)
     * @return {@code 202} with {@code jobId} and {@code soundId}
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> triggerUploadJob(
            @RequestParam("soundId") Integer soundId,
            @RequestParam("file") MultipartFile file) {

        // Verify the sound exists before queuing
        Optional<Sound> optSound = soundRepository.findById(soundId);
        if (optSound.isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Sound not found: " + soundId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }

        try {
            String jobId = UUID.randomUUID().toString();
            String audioBase64 = Base64.getEncoder().encodeToString(file.getBytes());
            String contentType = file.getContentType() != null ? file.getContentType() : "audio/mpeg";

            Map<String, String> payload = new HashMap<>();
            payload.put("jobId", jobId);
            payload.put("soundId", soundId.toString());
            payload.put("audioBase64", audioBase64);
            payload.put("contentType", contentType);
            payload.put("status", "RECEIVED");

            audioJobPublisher.publishUploadJob(payload);
            LOGGER.info("Queued upload job {} for soundId {}", jobId, soundId);

            Map<String, String> response = new HashMap<>();
            response.put("jobId", jobId);
            response.put("soundId", soundId.toString());
            response.put("message", "Upload job queued successfully.");
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch (Exception e) {
            LOGGER.error("Failed to queue upload job for soundId {}: {}", soundId, e.getMessage(), e);
            Map<String, String> err = new HashMap<>();
            err.put("error", "Failed to read file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    // -------------------------------------------------------------------------
    // POST /audio/download/{soundId}
    // -------------------------------------------------------------------------

    /**
     * Queues a download job for the given Sound.
     *
     * <p>
     * Returns {@code 202 Accepted} with a {@code jobId}. The client should poll
     * {@code GET /audio/download/status/{jobId}} until the audio is ready.
     *
     * @param soundId the ID of the Sound to retrieve
     * @return {@code 202} with {@code jobId}; {@code 404} if sound not found
     */
    @PostMapping("/download/{soundId}")
    public ResponseEntity<Map<String, String>> triggerDownloadJob(
            @PathVariable("soundId") Integer soundId) {

        Optional<Sound> optSound = soundRepository.findById(soundId);
        if (optSound.isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Sound not found: " + soundId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }

        String jobId = UUID.randomUUID().toString();

        Map<String, String> payload = new HashMap<>();
        payload.put("jobId", jobId);
        payload.put("soundId", soundId.toString());

        audioJobPublisher.publishRetrievalJob(payload);
        LOGGER.info("Queued download job {} for soundId {}", jobId, soundId);

        Map<String, String> response = new HashMap<>();
        response.put("jobId", jobId);
        response.put("message", "Download job queued. Poll /audio/download/status/" + jobId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    // -------------------------------------------------------------------------
    // GET /audio/download/status/{jobId} — polling endpoint
    // -------------------------------------------------------------------------

    /**
     * Polls Redis for the result of a previously queued download job.
     *
     * <p>
     * Returns {@code 200} with Base-64 audio once ready, {@code 202} while
     * still processing, or {@code 500} on worker error.
     *
     * @param jobId the correlation ID returned by {@link #triggerDownloadJob}
     * @return status response
     */
    @GetMapping("/download/status/{jobId}")
    public ResponseEntity<Map<String, String>> getDownloadStatus(
            @PathVariable("jobId") String jobId) {

        Object result = redisTemplate.opsForValue().get("ready-audio:" + jobId);

        Map<String, String> response = new HashMap<>();

        if (result == null) {
            response.put("status", "PROCESSING");
            response.put("jobId", jobId);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        }

        String value = result.toString();

        if (value.startsWith("ERROR:")) {
            response.put("status", "ERROR");
            response.put("detail", value.substring(6));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        response.put("status", "READY");
        response.put("jobId", jobId);
        response.put("audioBase64", value);
        return ResponseEntity.ok(response);
    }
}
