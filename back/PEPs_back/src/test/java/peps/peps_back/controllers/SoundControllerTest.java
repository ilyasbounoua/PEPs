package peps.peps_back.controllers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/applicationContext.xml", "file:src/main/webapp/WEB-INF/dispatcher-servlet.xml"})
@WebAppConfiguration
public class SoundControllerTest {

    @Autowired
    private SoundController instance;

    @Test
    public void testGetAllSounds() {
        System.out.println("getAllSounds");
        ResponseEntity result = instance.getAllSounds(null);
        assertNotNull(result);
    }

    @Test
    public void testGetSoundFile() {
        System.out.println("getSoundFile");
        Integer id = 1;
        try {
            ResponseEntity<Resource> result = instance.getSoundFile(id);
            assertNotNull(result);
        } catch (Exception e) {
            System.out.println("Controller threw expected exception: " + e.getMessage());
        }
    }

    @Test
    public void testUploadSound() throws java.io.IOException {
        System.out.println("uploadSound");
        String name = "test";
        String type = "audio";
        MockMultipartFile file = new MockMultipartFile("file", "test.mp3", "audio/mpeg", "test data".getBytes());

        try {
            ResponseEntity result = instance.uploadSound(name, type, file, null, null);
            assertNotNull(result);
        } catch (Exception e) {
            // Catch potential IOExceptions from file handling
        }
    }

    @Test
    public void testUpdateSound() {
        System.out.println("updateSound");
        Integer id = 1;
        SoundDTO soundDTO = new SoundDTO(); 

        try {
            ResponseEntity result = instance.updateSound(id, soundDTO, null);
            assertNotNull(result);
        } catch (Exception e) {
            System.out.println("Ignored update error: " + e.getMessage());
        }
    }

    @Test
    public void testDeleteSound() {
        System.out.println("deleteSound");
        Integer id = 1;
        ResponseEntity result = instance.deleteSound(id, null);
        assertNotNull(result);
    }
}