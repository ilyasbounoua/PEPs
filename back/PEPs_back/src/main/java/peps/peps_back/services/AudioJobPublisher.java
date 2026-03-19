package peps.peps_back.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service EXCLUSIVELY responsible for publishing (sending) messages to Redis
 * Streams.
 * It does not decide or process business logic; it acts only as a bridge to
 * queue jobs.
 * 
 * @author Santiago Alexander RODRIGUEZ TRIANA
 */
@Service
public class AudioJobPublisher {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Queues a job in the "upload-audio-stream" (Pipeline for uploading to MinIO).
     * 
     * @param data The payload containing job details (e.g., jobId, file reference).
     */
    public void publishUploadJob(Map<String, String> data) {
        redisTemplate.opsForStream().add("upload-audio-stream", data);
    }

    /**
     * Queues a job in the "retrieval-request-stream" (Pipeline for downloading to
     * ESP32).
     * 
     * @param data The payload containing job details (e.g., fileId to fetch).
     */
    public void publishRetrievalJob(Map<String, String> data) {
        redisTemplate.opsForStream().add("retrieval-request-stream", data);
    }
}
