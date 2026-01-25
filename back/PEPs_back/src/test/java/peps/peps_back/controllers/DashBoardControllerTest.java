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
import org.springframework.http.ResponseEntity;

/**
 *
 * @author Clément
 */
public class DashBoardControllerTest {

    public DashBoardControllerTest() {
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
     * Test of dashboard method, of class DashBoardController.
     */
    @Test
    public void testDashboard() {
        System.out.println("dashboard");
        DashBoardController instance = new DashBoardController();
        ResponseEntity<DashboardStats> expResult = null;
        // Updated to match new signature: dashboard(role, startDate, endDate)
        ResponseEntity<DashboardStats> result = instance.dashboard(null, null, null);
        assertEquals(expResult, result);
    }

}
