package peps.peps_back.workers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

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
import peps.peps_back.items.Sound;
import peps.peps_back.repositories.SoundRepository;
import peps.peps_back.services.MinioStorageService;

/**
 * Unit tests for StorageWorker.
 */
public class StorageWorkerTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @Mock
    private MinioStorageService minioStorageService;

    @Mock
    private SoundRepository soundRepository;

    @InjectMocks
    private StorageWorker storageWorker;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Mock de la partie Stream de Redis
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
    }

    @Test
    @DisplayName("Should process message, upload to MinIO and update database successfully")
    public void testOnMessage_Success() throws Exception {
        // 1. Préparation des données du message Redis
        String soundId = "10";
        String audioContent = "fake-audio-data";
        String audioBase64 = Base64.getEncoder().encodeToString(audioContent.getBytes());
        
        Map<String, String> payload = Map.of(
            "soundId", soundId,
            "audioBase64", audioBase64,
            "contentType", "audio/mpeg",
            "soundName", "test-sound"
        );
        
        // Création d'un MapRecord simulé
        MapRecord<String, String, String> message = MapRecord.create("storage-upload-stream", payload)
                .withId(RecordId.of("123-0"));

        // 2. Mock des dépendances
        Sound mockSound = new Sound();
        mockSound.setIdsound(10);
        mockSound.setNom("test-sound");
        mockSound.setTypeSon("AMBIANCE");
        mockSound.setExtension("mp3");

        when(soundRepository.findById(10)).thenReturn(Optional.of(mockSound));
        when(minioStorageService.uploadSound(any(), any(), any(), any(), any(InputStream.class), anyLong()))
            .thenReturn("minio-object-key-123");

        // 3. Exécution
        storageWorker.onMessage(message);

        // 4. Vérifications
        // Vérifie l'upload MinIO avec les bons paramètres
        verify(minioStorageService).uploadSound(
            eq("test-sound"), eq("AMBIANCE"), eq("mp3"), eq("audio/mpeg"), any(InputStream.class), eq((long)audioContent.getBytes().length)
        );

        // Vérifie la mise à jour en BDD
        verify(soundRepository).save(argThat(sound -> 
            sound.getChemin().equals("minio-object-key-123")
        ));

        // Vérifie l'accusé de réception Redis (ACK)
        verify(streamOperations).acknowledge(eq("storage-upload-stream"), eq("storage-handlers"), eq(message.getId()));
    }

    @Test
    @DisplayName("Should skip processing and ACK if required fields are missing")
    public void testOnMessage_MissingFields() {
        // Message vide
        MapRecord<String, String, String> message = MapRecord.create("stream", Map.of("soundName", "bad-msg"))
                .withId(RecordId.of("124-0"));

        storageWorker.onMessage(message);

        // Ne doit pas appeler MinIO ni le Repo
        verifyNoInteractions(minioStorageService);
        verify(soundRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should skip processing and ACK if sound is not found in database")
    public void testOnMessage_SoundNotFound() {
        MapRecord<String, String, String> message = MapRecord.create("stream", Map.of(
            "soundId", "99",
            "audioBase64", "some-data"
        )).withId(RecordId.of("125-0"));

        when(soundRepository.findById(99)).thenReturn(Optional.empty());

        storageWorker.onMessage(message);

        verifyNoInteractions(minioStorageService);
    }

    @Test
    @DisplayName("Should always ACK even if an exception occurs during processing")
    public void testOnMessage_WithException() {
        MapRecord<String, String, String> message = MapRecord.create("stream", Map.of(
            "soundId", "1",
            "audioBase64", "valid-base64"
        )).withId(RecordId.of("126-0"));

        when(soundRepository.findById(1)).thenThrow(new RuntimeException("Database error"));

        storageWorker.onMessage(message);

        // Vérifie que le bloc finally a bien exécuté l'ACK malgré l'exception
        verify(streamOperations).acknowledge(any(), any(), eq(message.getId()));
    }
}