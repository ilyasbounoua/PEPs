package peps.peps_back.workers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Base64;
import java.util.Map;

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

/**
 * Tests unitaires pour RawAudioWorker.
 * Vérifie le décodage Base64, la validation des signatures binaires (Magic Bytes)
 * et le transfert vers le stream suivant.
 */
public class RawAudioWorkerTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @InjectMocks
    private RawAudioWorker rawAudioWorker;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
    }

    @Test
    @DisplayName("Validation: Should forward valid WAV file to storage-upload-stream")
    public void testOnMessage_ValidWav() {
        // Signature WAV (RIFF) : 52 49 46 46 + dummy data pour atteindre 12 bytes
        byte[] wavBytes = new byte[]{0x52, 0x49, 0x46, 0x46, 0, 0, 0, 0, 0, 0, 0, 0};
        String base64 = Base64.getEncoder().encodeToString(wavBytes);

        Map<String, String> payload = Map.of(
            "soundId", "1",
            "audioBase64", base64,
            "soundName", "test.wav"
        );
        MapRecord<String, String, String> message = MapRecord.create("upload-audio-stream", payload)
                .withId(RecordId.of("1-0"));

        rawAudioWorker.onMessage(message);

        // Vérifie que le message est poussé vers le stream suivant
        verify(streamOperations).add(eq("storage-upload-stream"), eq(payload));
        // Vérifie l'ACK
        verify(streamOperations).acknowledge(anyString(), anyString(), eq(message.getId()));
    }

    @Test
    @DisplayName("Validation: Should forward valid MP3 (ID3) file")
    public void testOnMessage_ValidMp3ID3() {
        // Signature MP3 ID3 : 49 44 33 (ID3)
        byte[] mp3Bytes = new byte[]{0x49, 0x44, 0x33, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        String base64 = Base64.getEncoder().encodeToString(mp3Bytes);

        MapRecord<String, String, String> message = MapRecord.create("stream", Map.of("audioBase64", base64))
                .withId(RecordId.of("2-0"));

        rawAudioWorker.onMessage(message);

        verify(streamOperations).add(eq("storage-upload-stream"), any());
    }

    @Test
    @DisplayName("Validation: Should reject invalid format (Random bytes)")
    public void testOnMessage_InvalidFormat() {
        // Bytes aléatoires qui ne correspondent à aucune signature connue
        byte[] randomBytes = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10, 0x11};
        String base64 = Base64.getEncoder().encodeToString(randomBytes);

        MapRecord<String, String, String> message = MapRecord.create("stream", Map.of("audioBase64", base64))
                .withId(RecordId.of("3-0"));

        rawAudioWorker.onMessage(message);

        // Ne doit PAS être envoyé au stream suivant
        verify(streamOperations, never()).add(anyString(), any());
    }

    @Test
    @DisplayName("Validation: Should reject malformed Base64")
    public void testOnMessage_InvalidBase64() {
        MapRecord<String, String, String> message = MapRecord.create("stream", Map.of("audioBase64", "!!!NotBase64!!!"))
                .withId(RecordId.of("4-0"));

        rawAudioWorker.onMessage(message);

        verify(streamOperations, never()).add(anyString(), any());
    }

    @Test
    @DisplayName("Validation: Should reject empty or null payload")
    public void testOnMessage_EmptyPayload() {
        MapRecord<String, String, String> message = MapRecord.create("stream", Map.of("audioBase64", ""))
                .withId(RecordId.of("5-0"));

        rawAudioWorker.onMessage(message);

        verify(streamOperations, never()).add(anyString(), any());
    }

    @Test
    @DisplayName("Validation: Should support M4A (ftyp at offset 4)")
    public void testOnMessage_ValidM4A() {
        // Signature M4A : "ftyp" (66 74 79 70) à partir du 4ème byte
        byte[] m4aBytes = new byte[]{0, 0, 0, 0, 0x66, 0x74, 0x79, 0x70, 0, 0, 0, 0};
        String base64 = Base64.getEncoder().encodeToString(m4aBytes);

        MapRecord<String, String, String> message = MapRecord.create("stream", Map.of("audioBase64", base64))
                .withId(RecordId.of("6-0"));

        rawAudioWorker.onMessage(message);

        verify(streamOperations).add(eq("storage-upload-stream"), any());
    }
}