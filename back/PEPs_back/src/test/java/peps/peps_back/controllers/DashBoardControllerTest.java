/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
import peps.peps_back.items.Module;
import peps.peps_back.repositories.InteractionRepository;
import peps.peps_back.repositories.ModuleRepository;

/**
 *
 * @author Clément
 */
public class DashBoardControllerTest {
    
    private InteractionRepository interactionRepository;
    private ModuleRepository moduleRepository;
    private DashBoardController controller;

    @BeforeEach
    /**
     * Create mocks and add them in the controller
     */
    public void setUp() {
        interactionRepository = mock(InteractionRepository.class);
        moduleRepository = mock(ModuleRepository.class);

        controller = new DashBoardController(interactionRepository, moduleRepository);
    }

    static peps.peps_back.items.Module.ModuleConfig config;
        
    @BeforeAll
    public static void setUpModuleConfig() {
        config = new peps.peps_back.items.Module.ModuleConfig();
        config.setActif(true);
        config.setCurrentMode("REPEAT");
        config.setIdmodule(2);
        config.setIpAdress("0.0.0.0");
        config.setLastSeen(new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime());
        config.setNom("Best Module");
        config.setStatus("Active");
        config.setVolume(10);
    }
    
    /**
     * Test of dashboard method, of class DashBoardController.
     */
    @Test
    public void testDashboard() {
        System.out.println("dashboard");
        
        ArrayList<Interaction> listInter = new ArrayList<>();
        listInter.add(new Interaction(1, "sound", new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime()));
        long count = 1;
        when(interactionRepository.findAll()).thenReturn(listInter);
        when(interactionRepository.count()).thenReturn(count);

        ArrayList<Module> listModules = new ArrayList<>();
        Module module = new peps.peps_back.items.Module(config);
        listModules.add(module);
        when(moduleRepository.findAll()).thenReturn(listModules);
        
        ResponseEntity result = controller.dashboard();
        assertNotNull(result);
    }
    
}
