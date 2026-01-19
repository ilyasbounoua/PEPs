/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.repositories;

import java.util.Calendar;
import java.util.GregorianCalendar;
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
    
    public InteractionRepositoryTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testSomeMethod() {
        InteractionRepository mockRepository = mock(InteractionRepository.class);
        

        Interaction interac = new Interaction(1, "sound", new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime());
        when(mockRepository.findById(1)).thenReturn(interac);

        User result = userService.getUserById("123");
        assertEquals("John Doe", result.getName());
    }
    
}
