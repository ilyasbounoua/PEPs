/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.controllers;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import peps.peps_back.repositories.InteractionRepository;
import peps.peps_back.repositories.ModuleRepository;

@ExtendWith(MockitoExtension.class)
public class DashBoardControllerTest {

    @Mock
    private InteractionRepository interactionRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @InjectMocks
    private DashBoardController dashBoardController;

    @Test
    public void testDashboard() {
        System.out.println("dashboard");
        when(interactionRepository.count()).thenReturn(0L);
        when(moduleRepository.findAll()).thenReturn(Collections.emptyList());
        when(interactionRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<DashboardStats> result = dashBoardController.dashboard();

        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
    }
}
