/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.controllers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import peps.peps_back.items.Module;
import peps.peps_back.repositories.ModuleRepository;

@ExtendWith(MockitoExtension.class)
public class ModuleControllerTest {

    @Mock
    private ModuleRepository moduleRepository;

    @InjectMocks
    private ModuleController moduleController;

    @Test
    public void testGetAllModules() {
        System.out.println("getAllModules");
        when(moduleRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<List<ModuleDTO>> result = moduleController.getAllModules();

        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
    }

    @Test
    public void testGetModule() {
        System.out.println("getModule");
        Module module = new Module();
        module.setIdmodule(1);
        module.setNom("Test Module");
        module.setActif(true);
        module.setIpAdress("127.0.0.1");
        module.setVolume(50);
        module.setCurrentMode("Manuel");
        
        when(moduleRepository.findById(1)).thenReturn(Optional.of(module));

        ResponseEntity<ModuleDTO> result = moduleController.getModule(1);

        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
    }

    @Test
    public void testUpdateModule() {
        System.out.println("updateModule");
        Module module = new Module();
        module.setIdmodule(1);
        
        ModuleDTO dto = new ModuleDTO(1, "Test", "Test", "Actif", "127.0.0.1", new ModuleConfigDTO(50, "Manuel", true, false));

        when(moduleRepository.findById(1)).thenReturn(Optional.of(module));
        when(moduleRepository.save(module)).thenReturn(module);

        ResponseEntity<?> result = moduleController.updateModule(1, dto);

        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
    }

    @Test
    public void testCreateModule() {
        System.out.println("createModule");
        ModuleDTO dto = new ModuleDTO(1, "Test", "Test", "Actif", "127.0.0.1", new ModuleConfigDTO(50, "Manuel", true, false));
        Module module = new Module();

        when(moduleRepository.save(org.mockito.ArgumentMatchers.any(Module.class))).thenReturn(module);

        ResponseEntity<?> result = moduleController.createModule(dto);

        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
    }

    @Test
    public void testDeleteModule() {
        System.out.println("deleteModule");
        Module module = new Module();
        module.setIdmodule(1);
        
        when(moduleRepository.findById(1)).thenReturn(Optional.of(module));

        ResponseEntity<?> result = moduleController.deleteModule(1);

        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
    }
}
