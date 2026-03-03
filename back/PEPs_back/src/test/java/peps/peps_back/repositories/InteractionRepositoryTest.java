/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.repositories;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import peps.peps_back.items.Interaction;

/**
 *
 * @author Clément
 */
public class InteractionRepositoryTest {
    
    private InteractionRepository mockRepository;   
    
    
    @BeforeEach
    public void setUp() {
        mockRepository = mock(InteractionRepository.class);
        
    }


    @Test
    public void testSomeMethod() {        

        Interaction interac = new Interaction(1, "sound", new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime());
        Optional<Interaction> interactionOpt= Optional.of(interac);
        Integer id = 1;
         when(mockRepository.findById(id)).thenReturn(interactionOpt);

        Optional<Interaction> result = mockRepository.findById(id);
        assertTrue(result.isPresent());
        Interaction interactionType = result.get();
        assertEquals("sound", interactionType.getTypeinteraction());
    }
    
}
