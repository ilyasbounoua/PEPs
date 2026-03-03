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
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Clément
 */
public class IndexControllerTest {
    
    public IndexControllerTest() {
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
     * Test of handleIndexGet method, of class IndexController.
     */
    @Test
    public void testHandleIndexGet() {
        System.out.println("handleIndexGet");
        IndexController instance = new IndexController();
        ModelAndView result = instance.handleIndexGet();
        assertNotNull(result);
    }
    
}
