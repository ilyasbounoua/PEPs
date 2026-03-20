package peps.peps_back.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import peps.peps_back.items.Sound;
import peps.peps_back.repositories.SoundRepository;
import peps.peps_back.services.AudioJobPublisher;

/**
 * Tests unitaires pour RedisAudioController.
 */
public class RedisAudioControllerTest {

    @Mock
    private AudioJobPublisher audioJobPublisher;

    @Mock
    private SoundRepository soundRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisAudioController redisAudioController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Configuration du mock RedisTemplate pour supporter opsForValue()
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Upload: Should return 202 Accepted when file is valid")
    public void testTriggerUploadJob_Success() throws Exception {
        Integer soundId = 1;
        MockMultipartFile file = new MockMultipartFile("file", "test.mp3", "audio/mpeg", "fake audio content".getBytes());
        
        when(soundRepository.findById(soundId)).thenReturn(Optional.of(new Sound()));

        ResponseEntity<Map<String, String>> result = redisAudioController.triggerUploadJob(soundId, file);

        assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
        assertNotNull(result.getBody().get("jobId"));
        assertEquals("Upload job queued successfully.", result.getBody().get("message"));
        verify(audioJobPublisher).publishUploadJob(any());
    }

    @Test
    @DisplayName("Upload: Should return 404 when soundId does not exist")
    public void testTriggerUploadJob_SoundNotFound() {
        Integer soundId = 999;
        MockMultipartFile file = new MockMultipartFile("file", "test.mp3", "audio/mpeg", "content".getBytes());
        
        when(soundRepository.findById(soundId)).thenReturn(Optional.empty());

        ResponseEntity<Map<String, String>> result = redisAudioController.triggerUploadJob(soundId, file);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertTrue(result.getBody().get("error").contains("Sound not found"));
        verify(audioJobPublisher, never()).publishUploadJob(any());
    }

    @Test
    @DisplayName("Download: Should return 202 and queue retrieval job")
    public void testTriggerDownloadJob_Success() {
        Integer soundId = 1;
        when(soundRepository.findById(soundId)).thenReturn(Optional.of(new Sound()));

        ResponseEntity<Map<String, String>> result = redisAudioController.triggerDownloadJob(soundId);

        assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
        assertNotNull(result.getBody().get("jobId"));
        verify(audioJobPublisher).publishRetrievalJob(any());
    }

    @Test
    @DisplayName("Status: Should return PROCESSING when Redis result is null")
    public void testGetDownloadStatus_Processing() {
        String jobId = "test-job-id";
        when(valueOperations.get("ready-audio:" + jobId)).thenReturn(null);

        ResponseEntity<Map<String, String>> result = redisAudioController.getDownloadStatus(jobId);

        assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
        assertEquals("PROCESSING", result.getBody().get("status"));
    }

    @Test
    @DisplayName("Status: Should return READY with Base64 data when audio is ready")
    public void testGetDownloadStatus_Ready() {
        String jobId = "test-job-id";
        String fakeBase64 = "U29tZSBmYWtlIGF1ZGlv";
        when(valueOperations.get("ready-audio:" + jobId)).thenReturn(fakeBase64);

        ResponseEntity<Map<String, String>> result = redisAudioController.getDownloadStatus(jobId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("READY", result.getBody().get("status"));
        assertEquals(fakeBase64, result.getBody().get("audioBase64"));
    }

    @Test
    @DisplayName("Status: Should return 500 when Redis contains an ERROR message")
    public void testGetDownloadStatus_Error() {
        String jobId = "test-job-id";
        String errorMessage = "ERROR:MinIO connection failed";
        when(valueOperations.get("ready-audio:" + jobId)).thenReturn(errorMessage);

        ResponseEntity<Map<String, String>> result = redisAudioController.getDownloadStatus(jobId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("ERROR", result.getBody().get("status"));
        assertEquals("MinIO connection failed", result.getBody().get("detail"));
    }
}