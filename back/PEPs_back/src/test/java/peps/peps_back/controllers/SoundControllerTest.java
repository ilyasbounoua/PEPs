package peps.peps_back.controllers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import peps.peps_back.items.Sound;
import peps.peps_back.repositories.SoundRepository;
import peps.peps_back.repositories.UserRepository;
import peps.peps_back.services.AuditService;
import peps.peps_back.services.MinioStorageService;

/**
 * Unit tests for {@link SoundController}.
 *
 * @author Haytam BEN SRIBIT
 */
public class SoundControllerTest {

    private SoundRepository soundRepository;
    private AuditService auditService;
    private UserRepository userRepository;
    private MinioStorageService minioStorageService;
    private SoundController instance;

    /**
     * Initializes controller dependencies with mocks.
     */
    @BeforeEach
    public void setUp() {
        soundRepository = mock(SoundRepository.class);
        auditService = mock(AuditService.class);
        userRepository = mock(UserRepository.class);
        minioStorageService = mock(MinioStorageService.class);

        instance = new SoundController(soundRepository, minioStorageService);
        ReflectionTestUtils.setField(instance, "auditService", auditService);
        ReflectionTestUtils.setField(instance, "userRepository", userRepository);
    }

    /**
     * Validates that listing sounds returns a non-null response.
     */
    @Test
    public void testGetAllSounds() {
        Sound sound = new Sound(7, "gong", "Percussion", ".mp3");
        sound.setChemin("dossier/");
        List<Sound> sounds = new ArrayList<>();
        sounds.add(sound);
        when(soundRepository.findAll()).thenReturn(sounds);

        ResponseEntity<?> result = instance.getAllSounds(null);

        assertNotNull(result);
    }

    /**
     * Validates that sound file streaming returns a non-null response.
     */
    @Test
    public void testGetSoundFile() {
        Integer id = 7;

        Optional<Sound> sound = Optional.of(new Sound(7, "gong", "Percussion", ".mp3"));
        sound.get().setChemin("vocal/gong_123.mp3");

        when(soundRepository.findById(id)).thenReturn(sound);
        try {
            when(minioStorageService.downloadSound(any())).thenReturn(new byte[] { 1, 2, 3 });
        } catch (Exception e) {
            fail(e.getMessage());
        }

        ResponseEntity<Resource> result = instance.getSoundFile(id);

        assertNotNull(result);
    }

    /**
     * Validates that uploading a sound returns a non-null response.
     */
    @Test
    public void testUploadSound() {
        String name = "test";
        String type = "audio";
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.mp3");
        when(file.getSize()).thenReturn(3L);
        when(file.isEmpty()).thenReturn(false);
        try {
            when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] { 1, 2, 3 }));
            when(minioStorageService.uploadSound(any(), any(), any(), any(), any(), anyLong()))
                    .thenReturn("audio/test_123.mp3");
        } catch (Exception e) {
            fail(e.getMessage());
        }

        when(soundRepository.save(any())).thenAnswer(i -> {
            Sound s = (Sound) i.getArguments()[0];
            s.setIdsound(1);
            return s;
        });

        ResponseEntity<?> result = instance.uploadSound(name, type, file, "admin", null);

        assertNotNull(result);
    }

    /**
     * Validates that updating sound metadata returns a non-null response.
     */
    @Test
    public void testUpdateSound() {
        Integer id = 1;
        SoundDTO soundDTO = new SoundDTO(id, "updated", "audio", "mp3");

        Sound sound = new Sound(1, "gong", "Percussion", "mp3");
        when(soundRepository.findById(id)).thenReturn(Optional.of(sound));
        when(soundRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        ResponseEntity<?> result = instance.updateSound(id, soundDTO, null);

        assertNotNull(result);
    }

    /**
     * Validates that deleting a sound returns a non-null response.
     */
    @Test
    public void testDeleteSound() {
        Integer id = 1;

        Sound sound = new Sound(1, "gong", "Percussion", "mp3");
        sound.setChemin("audio/gong_123.mp3");
        when(soundRepository.findById(id)).thenReturn(Optional.of(sound));
        doNothing().when(soundRepository).delete(any(Sound.class));

        ResponseEntity<?> result = instance.deleteSound(id, null);

        assertNotNull(result);
    }
}
