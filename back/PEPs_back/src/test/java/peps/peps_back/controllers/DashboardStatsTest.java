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
public class DashboardStatsTest {
    
    public DashboardStatsTest() {
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
     * Test of getTotalInteractions method, of class DashboardStats.
     */
    @Test
    public void testGetTotalInteractions() {
        System.out.println("getTotalInteractions");
        DashboardStats instance = new DashboardStats();
        int expResult = 4;
        int result = instance.getTotalInteractions();
        assertEquals(expResult, result);
    }

    /**
     * Test of setTotalInteractions and getter method, of class DashboardStats.
     */
    @Test
    public void testGetSetTotalInteractions() {
        System.out.println("setTotalInteractions");
        int totalInteractions = 13;
        DashboardStats instance = new DashboardStats();
        instance.setTotalInteractions(totalInteractions);
        
        int result = instance.getTotalInteractions();
        assertEquals(totalInteractions, result);
    }

    /**
     * Test of getActiveModules and setActiveModules method, of class DashboardStats.
     */
    @Test
    public void testGetSetActiveModules() {
        System.out.println("setActiveModules");
        int activeModules = 3;
        DashboardStats instance = new DashboardStats();
        instance.setActiveModules(activeModules);
        
        int result = instance.getActiveModules();
        assertEquals(activeModules, result);
    }
    
    /**
     * Test of setLastInteraction method, of class DashboardStats.
     */
    @Test
    public void testGetSetLastInteraction() {
        System.out.println("setLastInteraction");
        String lastInteraction = "11/28/2025";
        DashboardStats instance = new DashboardStats();
        instance.setLastInteraction(lastInteraction);
        
        
        String result = instance.getLastInteraction();
        assertEquals(lastInteraction, result);
    }
    
}
