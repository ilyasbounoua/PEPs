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
import peps.peps_back.items.Module;

/**
 *
 * @author Clément
 */
public class ModuleRepositoryTest {
    
    private ModuleRepository mockRepository;   
    
    static peps.peps_back.items.Module.ModuleConfig config;
        
    @BeforeAll
    public static void setUpClass() {
        config = new peps.peps_back.items.Module.ModuleConfig();
        config.setActif(true);
        config.setCurrentMode("REPEAT");
        config.setIdmodule(2);
        config.setIpAdress("0.0.0.0");
        config.setLastSeen(new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime());
        config.setNom("Best Module");
        config.setStatus("Active");
        config.setVolume(10);
    }
    
    @BeforeEach
    public void setUp() {
        mockRepository = mock(ModuleRepository.class);
        
    }

    @Test
    public void testSomeMethod() {

        Module module = new Module(config);
        Optional<Module> interactionOpt= Optional.of(module);
        Integer id = 1;
         when(mockRepository.findById(id)).thenReturn(interactionOpt);

        Optional<peps.peps_back.items.Module> result = mockRepository.findById(id);
        assertTrue(result.isPresent());
        Module moduleType = result.get();
        assertEquals("REPEAT", moduleType.getCurrentMode());
    }
    
}
