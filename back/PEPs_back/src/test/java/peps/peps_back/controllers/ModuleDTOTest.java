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
public class ModuleDTOTest {
    
    public ModuleDTOTest() {
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
     * Test of getters and setters
     */
    @Test
    public void testGettersSettersId() {
        Integer id1 = 2;
        Integer id2 = 3;
        ModuleConfigDTO moduleConfig1 = new ModuleConfigDTO(10, "OFF", false, false);
        ModuleConfigDTO moduleConfig2 = new ModuleConfigDTO(10, "ON", false, false);
        
        ModuleDTO mdto = new ModuleDTO(id1, "m1", "z1", "OFF", "8.8.8.8", moduleConfig1);
        
        mdto.setConfig(moduleConfig2);
        mdto.setId(id2);
        mdto.setIp("1");
        mdto.setLocation("z2");
        mdto.setName("m2");
        mdto.setStatus("ON");
        
        mdto.getConfig();
        mdto.getId();
        mdto.getIp();
        mdto.getLocation();
        mdto.getName();
        mdto.getStatus();
    }
    
}
