/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.controllers;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import peps.peps_back.repositories.InteractionRepository;

@ExtendWith(MockitoExtension.class)
public class DailyStatsControllerTest {

    @Mock
    private InteractionRepository interactionRepository;

    @InjectMocks
    private DailyStatsController dailyStatsController;

    @Test
    public void testGetDailyStats() {
        System.out.println("getDailyStats");
        when(interactionRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<List<DailyDataDTO>> result = dailyStatsController.getDailyStats();

        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
    }
}
