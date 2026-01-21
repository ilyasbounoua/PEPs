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
public class DailyStatsControllerTest {

    public DailyStatsControllerTest() {
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
     * Test of getDailyStats method, of class DailyStatsController.
     */
    @Test
    public void testGetDailyStats() {
        System.out.println("getDailyStats");
        DailyStatsController instance = new DailyStatsController();
        ResponseEntity<List<DailyDataDTO>> expResult = null;
        ResponseEntity<List<DailyDataDTO>> result = instance.getDailyStats(null);
        assertEquals(expResult, result);
    }

}
