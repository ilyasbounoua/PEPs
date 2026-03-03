/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.repositories;


import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import peps.peps_back.items.Sound;

/**
 *
 * @author Clément
 */
public class SoundRepositoryTest {
    
    private SoundRepository mockRepository;   
    
    
    @BeforeEach
    public void setUp() {
        mockRepository = mock(SoundRepository.class);
        
    }


    @Test
    public void testSomeMethod() {        

        Sound sound = new Sound(7, "gong", "Percussion", ".mp3");
        Optional<Sound> soundOpt= Optional.of(sound);
        Integer id = 1;
         when(mockRepository.findById(id)).thenReturn(soundOpt);

        Optional<Sound> result = mockRepository.findById(id);
        assertTrue(result.isPresent());
        Sound soundType = result.get();
        assertEquals("Percussion", soundType.getTypeSon());
    }
    
}
