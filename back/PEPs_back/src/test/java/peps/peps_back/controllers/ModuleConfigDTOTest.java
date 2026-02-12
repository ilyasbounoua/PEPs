/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.controllers;

import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Clément
 */
public class ModuleConfigDTOTest {
    
    public ModuleConfigDTOTest() {
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

    /**
     * int volume, String mode, boolean actif, boolean son
     * Test of getVolume method, of class ModuleConfigDTO.
     */
    @Test
    public void testGettersSetters() {
        System.out.println("getVolume");
        ModuleConfigDTO moduleConfig = new ModuleConfigDTO(10, "OFF", false, false);
        
        moduleConfig.setVolume(7);
        moduleConfig.setMode("ON");
        moduleConfig.setActif(true);
        moduleConfig.setSon(true);
        
        assertEquals(moduleConfig.getVolume(),7);
        assertEquals(moduleConfig.getMode(), "ON");
        assertEquals(moduleConfig.isActif(),true);
        assertEquals(moduleConfig.isSon(),true);
        
    }

}
