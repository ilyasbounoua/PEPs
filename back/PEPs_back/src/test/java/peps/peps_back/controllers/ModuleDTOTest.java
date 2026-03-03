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
public class ModuleDTOTest {
    
    public ModuleDTOTest() {
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
     * Test of getId method, of class ModuleDTO.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");
        ModuleDTO instance = new ModuleDTO();
        Integer expResult = null;
        Integer result = instance.getId();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setId method, of class ModuleDTO.
     */
    @Test
    public void testSetId() {
        System.out.println("setId");
        Integer id = null;
        ModuleDTO instance = new ModuleDTO();
        instance.setId(id);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getName method, of class ModuleDTO.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        ModuleDTO instance = new ModuleDTO();
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setName method, of class ModuleDTO.
     */
    @Test
    public void testSetName() {
        System.out.println("setName");
        String name = "";
        ModuleDTO instance = new ModuleDTO();
        instance.setName(name);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLocation method, of class ModuleDTO.
     */
    @Test
    public void testGetLocation() {
        System.out.println("getLocation");
        ModuleDTO instance = new ModuleDTO();
        String expResult = "";
        String result = instance.getLocation();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLocation method, of class ModuleDTO.
     */
    @Test
    public void testSetLocation() {
        System.out.println("setLocation");
        String location = "";
        ModuleDTO instance = new ModuleDTO();
        instance.setLocation(location);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStatus method, of class ModuleDTO.
     */
    @Test
    public void testGetStatus() {
        System.out.println("getStatus");
        ModuleDTO instance = new ModuleDTO();
        String expResult = "";
        String result = instance.getStatus();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setStatus method, of class ModuleDTO.
     */
    @Test
    public void testSetStatus() {
        System.out.println("setStatus");
        String status = "";
        ModuleDTO instance = new ModuleDTO();
        instance.setStatus(status);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIp method, of class ModuleDTO.
     */
    @Test
    public void testGetIp() {
        System.out.println("getIp");
        ModuleDTO instance = new ModuleDTO();
        String expResult = "";
        String result = instance.getIp();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setIp method, of class ModuleDTO.
     */
    @Test
    public void testSetIp() {
        System.out.println("setIp");
        String ip = "";
        ModuleDTO instance = new ModuleDTO();
        instance.setIp(ip);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConfig method, of class ModuleDTO.
     */
    @Test
    public void testGetConfig() {
        System.out.println("getConfig");
        ModuleDTO instance = new ModuleDTO();
        ModuleConfigDTO expResult = null;
        ModuleConfigDTO result = instance.getConfig();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setConfig method, of class ModuleDTO.
     */
    @Test
    public void testSetConfig() {
        System.out.println("setConfig");
        ModuleConfigDTO config = null;
        ModuleDTO instance = new ModuleDTO();
        instance.setConfig(config);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
