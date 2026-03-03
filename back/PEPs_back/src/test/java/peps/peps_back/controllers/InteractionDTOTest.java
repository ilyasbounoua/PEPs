/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.controllers;

import java.util.Date;
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
public class InteractionDTOTest {
    
    public InteractionDTOTest() {
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
     * Test of getId method, of class InteractionDTO.
     */
    @Test
    public void testGetSetId() {
        System.out.println("getId");
        InteractionDTO instance = null;
        Integer expResult = null;
        Integer result = instance.getId();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setId method, of class InteractionDTO.
     */
    @Test
    public void testSetId() {
        System.out.println("setId");
        Integer id = null;
        InteractionDTO instance = null;
        instance.setId(id);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDate method, of class InteractionDTO.
     */
    @Test
    public void testGetDate() {
        System.out.println("getDate");
        InteractionDTO instance = null;
        Date expResult = null;
        Date result = instance.getDate();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDate method, of class InteractionDTO.
     */
    @Test
    public void testSetDate() {
        System.out.println("setDate");
        Date date = null;
        InteractionDTO instance = null;
        instance.setDate(date);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getModule method, of class InteractionDTO.
     */
    @Test
    public void testGetModule() {
        System.out.println("getModule");
        InteractionDTO instance = null;
        String expResult = "";
        String result = instance.getModule();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModule method, of class InteractionDTO.
     */
    @Test
    public void testSetModule() {
        System.out.println("setModule");
        String module = "";
        InteractionDTO instance = null;
        instance.setModule(module);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getType method, of class InteractionDTO.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");
        InteractionDTO instance = null;
        String expResult = "";
        String result = instance.getType();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setType method, of class InteractionDTO.
     */
    @Test
    public void testSetType() {
        System.out.println("setType");
        String type = "";
        InteractionDTO instance = null;
        instance.setType(type);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
