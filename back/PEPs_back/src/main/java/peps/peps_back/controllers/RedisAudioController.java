package peps.peps_back.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import peps.peps_back.services.AudioJobPublisher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for interacting with the Audio Infrastructure.
 * Only active in the "backend-audio" process thanks to the Profile annotation.
 * This ensures resource isolation and traffic isolation (Nginx routing here for
 * /api/audio/*).
 * 
 * @author Santiago Alexander RODRIGUEZ TRIANA
 */
@RestController
@RequestMapping("/audio")
@Profile("audio-worker")
public class RedisAudioController {

    @Autowired
    private AudioJobPublisher audioJobPublisher;

    /**
     * Endpoint to upload audio from a client to the architecture.
     * Returns a 202 Accepted quickly, leaving the heavy lifting to the Workers.
     * 
     * @return Confirmation with jobId.
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> triggerUploadJob() {
        String jobId = UUID.randomUUID().toString();

        Map<String, String> payload = new HashMap<>();
        payload.put("jobId", jobId);
        payload.put("status", "RECEIVED");
        // TODO: Assign the actual audio bytes arriving from the HTTP client here.

        audioJobPublisher.publishUploadJob(payload);

        Map<String, String> response = new HashMap<>();
        response.put("jobId", jobId);
        response.put("message", "Upload job queued successfully.");

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Endpoint for the ESP32 to request the download of an audio file.
     * 
     * @param fileId the ID of the file to render and return.
     * @return A status message for now.
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<String> triggerDownloadJob(@PathVariable("id") String fileId) {
        String jobId = UUID.randomUUID().toString();

        Map<String, String> payload = new HashMap<>();
        payload.put("jobId", jobId);
        payload.put("fileId", fileId);

        // The event is fired to the stream. This is asynchronous by nature.
        audioJobPublisher.publishRetrievalJob(payload);

        // TODO: True TCP/HTTP response integration.
        // Since Redis Streams are asynchronous, there are two possible approaches here:
        // 1. Long-polling (Stay in a while-loop checking Redis until the
        // DevicePrepWorker
        // inserts the "ready-audio:123" key with the byte array).
        // 2. 202-Accepted Pattern (The ESP32 receives a 202 and asks another endpoint
        // minutes later if the audio is ready in the redis cache).

        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Download job queued. Check back later.");
    }
}
