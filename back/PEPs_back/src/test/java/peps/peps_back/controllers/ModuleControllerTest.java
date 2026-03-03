/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.controllers;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author Clément
 */
public class ModuleControllerTest {

    public ModuleControllerTest() {
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
     * Test of getAllModules method, of class ModuleController.
     */
    @Test
    public void testGetAllModules() {
        System.out.println("getAllModules");
        ModuleController instance = new ModuleController();
        ResponseEntity<List<ModuleDTO>> expResult = null;
        ResponseEntity<List<ModuleDTO>> result = instance.getAllModules(null);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getModule method, of class ModuleController.
     */
    @Test
    public void testGetModule() {
        System.out.println("getModule");
        Integer id = null;
        ModuleController instance = new ModuleController();
        ResponseEntity<ModuleDTO> expResult = null;
        ResponseEntity<ModuleDTO> result = instance.getModule(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of updateModule method, of class ModuleController.
     */
    @Test
    public void testUpdateModule() {
        System.out.println("updateModule");
        Integer id = null;
        ModuleDTO dto = null;
        ModuleController instance = new ModuleController();
        ResponseEntity expResult = null;
        ResponseEntity result = instance.updateModule(id, dto, null);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createModule method, of class ModuleController.
     */
    @Test
    public void testCreateModule() {
        System.out.println("createModule");
        ModuleDTO dto = null;
        ModuleController instance = new ModuleController();
        ResponseEntity expResult = null;
        ResponseEntity result = instance.createModule(dto, null, null);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteModule method, of class ModuleController.
     */
    @Test
    public void testDeleteModule() {
        System.out.println("deleteModule");
        Integer id = null;
        ModuleController instance = new ModuleController();
        ResponseEntity expResult = null;
        ResponseEntity result = instance.deleteModule(id, null);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
