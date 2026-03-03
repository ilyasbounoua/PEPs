package peps.peps_back.controllers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DashboardStatsTest {

    /**
     * Test of getTotalInteractions method, of class DashboardStats.
     */
    @Test
    public void testGetTotalInteractions() {
        System.out.println("getTotalInteractions");
        DashboardStats instance = new DashboardStats();
        int expResult = 0;
        int result = instance.getTotalInteractions();
        assertEquals(expResult, result);
    }

    /**
     * Test of setTotalInteractions and getter method, of class DashboardStats.
     */
    @Test
    public void testGetSetTotalInteractions() {
        System.out.println("get/set totalInteractions");
        DashboardStats instance = new DashboardStats(13, 3, "11/28/2025");
        int totalInteractions = 13;
        instance.setTotalInteractions(totalInteractions);
        
        int result = instance.getTotalInteractions();
        assertEquals(totalInteractions, result);
    }

    @Test
    public void testGetSetActiveModules() {
        System.out.println("get/set activeModules");
        DashboardStats instance = new DashboardStats(13, 3, "11/28/2025");
        int activeModules = 3;
        instance.setActiveModules(activeModules);
        
        int result = instance.getActiveModules();
        assertEquals(activeModules, result);
    }
    
    @Test
    public void testGetSetLastInteraction() {
        System.out.println("get/set lastInteraction");
        DashboardStats instance = new DashboardStats(13, 3, "11/28/2025");
        String lastInteraction = "11/28/2025";
        instance.setLastInteraction(lastInteraction);
        
        String result = instance.getLastInteraction();
        assertEquals(lastInteraction, result);
    }
    
}
