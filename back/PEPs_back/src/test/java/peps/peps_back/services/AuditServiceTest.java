/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import peps.peps_back.controllers.SoundController;
import peps.peps_back.repositories.AuditLogRepository;
import peps.peps_back.repositories.SoundRepository;

/**
 *
 * @author Clément
 */
public class AuditServiceTest {
    
    public AuditServiceTest() {
    }
    
    private AuditLogRepository auditLogRepository;
    private AuditService instance;
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
        // 1. Create the Mock
        auditLogRepository = mock(AuditLogRepository.class);
        
        // 2. Inject Mock into Controller
        instance = new AuditService(auditLogRepository);
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of log method, of class AuditService.
     */
    @Test
    public void testLog() {
        System.out.println("log");
        String action = "Obs";
        String entityType = "Ara";
        Integer entityId = 1;
        String entityName = "Pouci";
        String entityRole = "oizeau";
        String userLogin = "i";
        String oldValue = "pouf";
        String newValue = "patapouf";
        String details = "oooooo";
        
        doNothing().when(auditLogRepository).save(any());
        
        instance.log(action, entityType, entityId, entityName, entityRole, userLogin, oldValue, newValue, details);

    }

    /**
     * Test of maskPassword method, of class AuditService.
     */
    @Test
    public void testMaskPassword() {
        System.out.println("maskPassword");
        String jsonValue1 = null;
        String expResult1 = null;
        String result1 = instance.maskPassword(jsonValue1);
        assertEquals(expResult1, result1);

        String jsonValue2 = "oijreijogjogroirjoreojrgoireoijroij";
        instance.maskPassword(jsonValue2);
    }
    
}
