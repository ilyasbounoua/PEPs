/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.ResponseEntity;
import peps.peps_back.items.Module;
import peps.peps_back.repositories.InteractionRepository;
import peps.peps_back.repositories.ModuleRepository;

/**
 *
 * @author Clément
 */
public class ModuleControllerTest {
    
    private ModuleRepository moduleRepository;
    private ModuleController controller;

    @BeforeEach
    /**
     * Create mocks and add them in the controller
     */
    public void setUp() {
        moduleRepository = mock(ModuleRepository.class);

        controller = new ModuleController(moduleRepository);
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
     * Test of getAllModules method, of class ModuleController.
     */
    @Test
    public void testGetAllModules() {
        System.out.println("getAllModules");

        ArrayList<peps.peps_back.items.Module> listModules = new ArrayList<>();
        peps.peps_back.items.Module module = new peps.peps_back.items.Module(config);
        listModules.add(module);
        when(moduleRepository.findAll()).thenReturn(listModules);
        
        String role = null;
        ResponseEntity result = controller.getAllModules(role);
        assertNotNull(result);
        
    }

    /**
     * Test of getModule method, of class ModuleController.
     */
    @Test
    public void testGetModule() {
        System.out.println("getModule");
        
        Optional<Module> module = Optional.of(new Module(config));
        Integer id = 2;
        when(moduleRepository.findById(id)).thenReturn(module);
        
        ResponseEntity result = controller.getModule(id);
        assertNotNull(result);
    }

    /**
     * Test of updateModule method, of class ModuleController.
     */
    @Test
    public void testUpdateModule() {
        System.out.println("updateModule");

        Optional<Module> module = Optional.of(new Module(config));
        Integer id = 2;
        when(moduleRepository.findById(id)).thenReturn(module);
        
        when(moduleRepository.save(any(Module.class))).thenReturn(module.orElse(null));
        
        ModuleConfigDTO config =  new ModuleConfigDTO(10, "OFF", false, false);
        ModuleDTO dto = new ModuleDTO(2, "module", "zone1", "active", "8.8.8.8", config);
                
        String login = null;
        ResponseEntity result = controller.updateModule(id, dto, login);
        assertNotNull(result);
    }

    /**
     * Test of createModule method, of class ModuleController.
     */
    @Test
    public void testCreateModule() {
        System.out.println("createModule");

        Module module = new Module(config);
        assertNotNull(module);
        when(moduleRepository.save(any(Module.class))).thenReturn(module);
        
        ModuleConfigDTO config =  new ModuleConfigDTO(10, "OFF", false, false);
        ModuleDTO dto = new ModuleDTO(2, "module", "zone1", "active", "8.8.8.8", config);
        
        String role = "admin";
        String login = null;
                
        ResponseEntity result = controller.createModule(dto, role, login);
        assertNotNull(result);
    }

    /**
     * Test of deleteModule method, of class ModuleController.
     */
    @Test
    public void testDeleteModule() {
        System.out.println("deleteModule");

        Optional<Module> module = Optional.of(new Module(config));
        Integer id = 2;
        when(moduleRepository.findById(id)).thenReturn(module);
        
        doNothing().when(moduleRepository).delete(module.orElse(null));
        
        String admin = null;
        ResponseEntity result = controller.deleteModule(id, admin);
        assertNotNull(result);
    }
    
}
