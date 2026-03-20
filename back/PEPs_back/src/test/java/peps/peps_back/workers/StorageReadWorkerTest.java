package peps.peps_back.workers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.ValueOperations;
import peps.peps_back.items.Sound;
import peps.peps_back.repositories.SoundRepository;
import peps.peps_back.services.MinioStorageService;

/**
 * Tests unitaires pour StorageReadWorker.
 */
public class StorageReadWorkerTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private MinioStorageService minioStorageService;

    @Mock
    private SoundRepository soundRepository;

    @InjectMocks
    private StorageReadWorker storageReadWorker;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Configuration des mocks pour les opérations Redis (Streams et Key-Value)
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Read: Should download from MinIO and forward to delivery-stream")
    public void testOnMessage_Success() throws Exception {
        // 1. Données simulées
        String jobId = "job-123";
        String soundId = "1";
        byte[] fakeAudioData = "audio-binary-content".getBytes();
        String expectedBase64 = Base64.getEncoder().encodeToString(fakeAudioData);

        MapRecord<String, String, String> message = MapRecord.create("retrieval-request-stream", 
            Map.of("jobId", jobId, "soundId", soundId))
            .withId(RecordId.of("1-0"));

        Sound mockSound = new Sound();
        mockSound.setIdsound(1);
        mockSound.setChemin("path/to/audio.mp3");
        mockSound.setExtension("mp3");

        // 2. Mocking
        when(soundRepository.findById(1)).thenReturn(Optional.of(mockSound));
        when(minioStorageService.downloadSound("path/to/audio.mp3")).thenReturn(fakeAudioData);

        // 3. Exécution
        storageReadWorker.onMessage(message);

        // 4. Vérifications
        // Vérifie que le payload envoyé au stream suivant contient le Base64
        verify(streamOperations).add(eq("device-delivery-stream"), argThat((Map<String, String> payload) -> 
            payload.get("audioBase64").equals(expectedBase64) && 
            payload.get("jobId").equals(jobId)
        ));

        // Vérifie l'ACK
        verify(streamOperations).acknowledge(anyString(), anyString(), eq(message.getId()));
    }

    @Test
    @DisplayName("Read: Should write ERROR to Redis if sound does not exist in DB")
    public void testOnMessage_SoundNotFound() {
        String jobId = "job-404";
        MapRecord<String, String, String> message = MapRecord.create("stream", 
            Map.of("jobId", jobId, "soundId", "999"))
            .withId(RecordId.of("2-0"));

        when(soundRepository.findById(999)).thenReturn(Optional.empty());

        storageReadWorker.onMessage(message);

        // Vérifie que l'erreur est écrite dans Redis pour le polling du frontend
        verify(valueOperations).set(eq("ready-audio:" + jobId), eq("ERROR:SOUND_NOT_FOUND"), eq(60L), eq(TimeUnit.SECONDS));
        verify(streamOperations).acknowledge(anyString(), anyString(), eq(message.getId()));
    }

    @Test
    @DisplayName("Read: Should write ERROR if sound has no path (not uploaded yet)")
    public void testOnMessage_NoPath() {
        String jobId = "job-500";
        MapRecord<String, String, String> message = MapRecord.create("stream", 
            Map.of("jobId", jobId, "soundId", "2"))
            .withId(RecordId.of("3-0"));

        Sound soundWithoutPath = new Sound();
        soundWithoutPath.setIdsound(2);
        soundWithoutPath.setChemin(null); // Pas encore d'upload MinIO

        when(soundRepository.findById(2)).thenReturn(Optional.of(soundWithoutPath));

        storageReadWorker.onMessage(message);

        verify(valueOperations).set(contains(jobId), eq("ERROR:NOT_UPLOADED_YET"), anyLong(), any());
        verify(streamOperations).acknowledge(anyString(), anyString(), eq(message.getId()));
    }

    @Test
    @DisplayName("Read: Should handle MinIO exceptions and write error to Redis")
    public void testOnMessage_MinioError() throws Exception {
        String jobId = "job-err";
        MapRecord<String, String, String> message = MapRecord.create("stream", 
            Map.of("jobId", jobId, "soundId", "1"))
            .withId(RecordId.of("4-0"));

        Sound sound = new Sound();
        sound.setChemin("valid/path");

        when(soundRepository.findById(1)).thenReturn(Optional.of(sound));
        when(minioStorageService.downloadSound(any())).thenThrow(new RuntimeException("MinIO down"));

        storageReadWorker.onMessage(message);

        // Vérifie que le message d'erreur contient l'exception
        verify(valueOperations).set(eq("ready-audio:" + jobId), contains("ERROR:MinIO down"), anyLong(), any());
        // L'ACK doit quand même avoir lieu pour ne pas bloquer le stream
        verify(streamOperations).acknowledge(anyString(), anyString(), eq(message.getId()));
    }
}