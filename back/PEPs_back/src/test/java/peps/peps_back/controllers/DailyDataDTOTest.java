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
public class DailyDataDTOTest {
    
    public DailyDataDTOTest() {
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
     * Test of getTime and setTime method, of class DailyDataDTO.
     */
    @Test
    public void testGetSetTime() {
        System.out.println("getTime");
        DailyDataDTO instance = new DailyDataDTO();
        String expResult = "25/12/2025:13:06:51";
        instance.setTime(expResult);
        String result = instance.getTime();
        assertEquals(expResult, result);
    }


    /**
     * Test of getCount and setCount method, of class DailyDataDTO.
     */
    @Test
    public void testGetCount() {
        System.out.println("getCount");
        DailyDataDTO instance = new DailyDataDTO();
        int expResult = 8;
        instance.setCount(expResult);
        int result = instance.getCount();
        assertEquals(expResult, result);
    }
    
}
