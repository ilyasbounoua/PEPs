/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.ResponseEntity;
import peps.peps_back.items.Interaction;
import peps.peps_back.repositories.InteractionRepository;

/**
 *
 * @author Clément
 */
public class InteractionControllerTest {
    
    private InteractionRepository interactionRepository;
    private InteractionController controller;

    @BeforeEach
    /**
     * Create mock and add it in the controller
     */
    public void setUp() {
        interactionRepository = mock(InteractionRepository.class);
        controller = new InteractionController(interactionRepository);
    }

    /**
     * Test of getAllInteractions method, of class InteractionController.
     */
    @Test
    public void testGetAllInteractions() {
        System.out.println("getAllInteractions");
        
        ArrayList<Interaction> listInter = new ArrayList<>();
        listInter.add(new Interaction(1, "sound", new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime()));
        when(interactionRepository.findAll()).thenReturn(listInter);
        
        ResponseEntity result = controller.getAllInteractions();
        assertNotNull(result);
    }
    
}
