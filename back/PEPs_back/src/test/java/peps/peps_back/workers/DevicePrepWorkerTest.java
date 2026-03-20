package peps.peps_back.workers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Base64;
import java.util.Map;
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

/**
 * Tests unitaires pour DevicePrepWorker.
 * Vérifie le choix de la stratégie de transcodage et le stockage final dans Redis.
 */
public class DevicePrepWorkerTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private DevicePrepWorker devicePrepWorker;

    private final String streamKey = "device-delivery-stream";
    private final String consumerGroup = "device-formatters";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("MP3 Strategy: Should pass MP3 bytes through unchanged for ESP32 native decoding")
    public void testOnMessage_Mp3PassThrough() {
        String jobId = "job-mp3";
        byte[] originalBytes = "fake-mp3-content".getBytes();
        String base64Input = Base64.getEncoder().encodeToString(originalBytes);

        Map<String, String> payload = Map.of(
            "jobId", jobId,
            "audioBase64", base64Input,
            "extension", "mp3"
        );
        MapRecord<String, String, String> message = MapRecord.create(streamKey, payload)
                .withId(RecordId.of("10-0"));

        devicePrepWorker.onMessage(message);

        // Vérifie que les octets stockés dans Redis sont EXACTEMENT les mêmes (Pass-through)
        verify(valueOperations).set(
            eq("ready-audio:" + jobId), 
            eq(base64Input), 
            eq(60L), 
            eq(TimeUnit.SECONDS)
        );

        // ACK explicite
        verify(streamOperations).acknowledge(eq(streamKey), eq(consumerGroup), eq(message.getId()));
    }

    @Test
    @DisplayName("WAV Strategy: Should attempt transcoding and store result in Redis")
    public void testOnMessage_WavTranscodingAttempt() {
        String jobId = "job-wav";
        // On simule un header WAV minimaliste (RIFF) pour que AudioSystem tente de le lire
        byte[] wavBytes = new byte[]{0x52, 0x49, 0x46, 0x46, 0, 0, 0, 0, 0, 0, 0, 0};
        String base64Input = Base64.getEncoder().encodeToString(wavBytes);

        MapRecord<String, String, String> message = MapRecord.create(streamKey, Map.of(
            "jobId", jobId,
            "audioBase64", base64Input,
            "extension", "wav"
        )).withId(RecordId.of("11-0"));

        devicePrepWorker.onMessage(message);

        // Ici, comme nos bytes sont bidons, AudioSystem.getAudioInputStream va probablement échouer.
        // Le code est censé catcher l'erreur et faire un "fallback" sur les bytes originaux.
        verify(valueOperations).set(eq("ready-audio:" + jobId), anyString(), anyLong(), any());
        
        // Vérifie l'ACK
        verify(streamOperations).acknowledge(eq(streamKey), eq(consumerGroup), eq(message.getId()));
    }

    @Test
    @DisplayName("Error Handling: Should write ERROR to Redis on exception and still ACK")
    public void testOnMessage_ExceptionHandling() {
        String jobId = "job-fail";
        // Payload corrompu (Base64 invalide) pour forcer une exception
        MapRecord<String, String, String> message = MapRecord.create(streamKey, Map.of(
            "jobId", jobId,
            "audioBase64", "not-base64-!!!"
        )).withId(RecordId.of("12-0"));

        devicePrepWorker.onMessage(message);

        // Vérifie que le marqueur d'erreur est écrit pour ne pas bloquer le client HTTP
        verify(valueOperations).set(eq("ready-audio:" + jobId), contains("ERROR:"), anyLong(), any());
        
        // IMPORTANT : Vérifie que le message est quand même ACK pour éviter une boucle infinie
        verify(streamOperations).acknowledge(eq(streamKey), eq(consumerGroup), eq(message.getId()));
    }

    @Test
    @DisplayName("Validation: Should skip and log error if jobId is missing")
    public void testOnMessage_MissingJobId() {
        MapRecord<String, String, String> message = MapRecord.create(streamKey, Map.of(
            "audioBase64", "some-data"
        )).withId(RecordId.of("13-0"));

        devicePrepWorker.onMessage(message);

        // Ne doit rien écrire dans Redis (car pas de jobId)
        verifyNoInteractions(valueOperations);
        // Mais ne doit pas ACK ou doit s'arrêter proprement selon la logique de sécurité
        // Ici le code actuel fait un "return", donc on ne vérifie pas l'ACK si on veut être strict,
        // mais dans votre code l'ACK est dans le "finally", donc il sera appelé.
        verify(streamOperations).acknowledge(anyString(), anyString(), any(RecordId.class));
    }
}