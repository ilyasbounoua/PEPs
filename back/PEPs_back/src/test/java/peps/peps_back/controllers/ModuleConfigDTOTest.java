/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.controllers;

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
     * Test of getVolume method, of class ModuleConfigDTO.
     */
    @Test
    public void testGetVolume() {
        System.out.println("getVolume");
        ModuleConfigDTO instance = new ModuleConfigDTO();
        int expResult = 0;
        int result = instance.getVolume();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setVolume method, of class ModuleConfigDTO.
     */
    @Test
    public void testSetVolume() {
        System.out.println("setVolume");
        int volume = 0;
        ModuleConfigDTO instance = new ModuleConfigDTO();
        instance.setVolume(volume);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMode method, of class ModuleConfigDTO.
     */
    @Test
    public void testGetMode() {
        System.out.println("getMode");
        ModuleConfigDTO instance = new ModuleConfigDTO();
        String expResult = "";
        String result = instance.getMode();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMode method, of class ModuleConfigDTO.
     */
    @Test
    public void testSetMode() {
        System.out.println("setMode");
        String mode = "";
        ModuleConfigDTO instance = new ModuleConfigDTO();
        instance.setMode(mode);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isActif method, of class ModuleConfigDTO.
     */
    @Test
    public void testIsActif() {
        System.out.println("isActif");
        ModuleConfigDTO instance = new ModuleConfigDTO();
        boolean expResult = false;
        boolean result = instance.isActif();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setActif method, of class ModuleConfigDTO.
     */
    @Test
    public void testSetActif() {
        System.out.println("setActif");
        boolean actif = false;
        ModuleConfigDTO instance = new ModuleConfigDTO();
        instance.setActif(actif);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isSon method, of class ModuleConfigDTO.
     */
    @Test
    public void testIsSon() {
        System.out.println("isSon");
        ModuleConfigDTO instance = new ModuleConfigDTO();
        boolean expResult = false;
        boolean result = instance.isSon();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSon method, of class ModuleConfigDTO.
     */
    @Test
    public void testSetSon() {
        System.out.println("setSon");
        boolean son = false;
        ModuleConfigDTO instance = new ModuleConfigDTO();
        instance.setSon(son);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
