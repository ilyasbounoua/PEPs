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
public class SoundDTOTest {
    
    public SoundDTOTest() {
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
     * Test of  method, of class SoundDTO.
     * 
     * We generate a soundDTO, gives him a name and an extension and then look if he have the correct file name combining both
     */
    @Test
    public void testgenerateFileName() {
        System.out.println("generateFileName");
        SoundDTO instance = new SoundDTO();
        String expResult = "music.mp4";
        
        String name = "music";
        String extension = "mp4";
        instance.setName(name);
        instance.setExtension(extension);
        String result = instance.getFileName();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getId method, of class SoundDTO.
     */
    @Test
    public void testGetSetId() {
        System.out.println("getId");
        SoundDTO instance = new SoundDTO();
        Integer expResult = 31;
        
        instance.setId(expResult);
        Integer result = instance.getId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getName method, of class SoundDTO.
     */
    @Test
    public void testGetSetName() {
        System.out.println("getName");
        SoundDTO instance = new SoundDTO();
        String expResult = "testName";
        
        instance.setName(expResult);
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getType method, of class SoundDTO.
     */
    @Test
    public void testGetSetType() {
        System.out.println("getType");
        SoundDTO instance = new SoundDTO();
        String expResult = " big sound";
        instance.setType(expResult);
        String result = instance.getType();
        assertEquals(expResult, result);
    }


    /**
     * Test of getExtension method, of class SoundDTO.
     */
    @Test
    public void testGetSetExtension() {
        System.out.println("getExtension");
        SoundDTO instance = new SoundDTO();
        String expResult = ".mp4";
        instance.setExtension(expResult);
        String result = instance.getExtension();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFileName method, of class SoundDTO.
     */
    @Test
    public void testGetSetFileName() {
        System.out.println("getFileName");
        SoundDTO instance = new SoundDTO();
        String expResult = "music1";
        
        instance.setFileName(expResult);
        String result = instance.getFileName();
        assertEquals(expResult, result);
    }

    
}
