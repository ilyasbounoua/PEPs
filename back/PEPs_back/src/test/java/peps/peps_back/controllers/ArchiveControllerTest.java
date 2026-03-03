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
import static org.mockito.Mockito.mock;
import org.springframework.http.ResponseEntity;
import peps.peps_back.repositories.AuditLogRepository;
import peps.peps_back.repositories.InteractionRepository;
import peps.peps_back.services.AuditService;

/**
 *
 * @author Clément
 */
public class ArchiveControllerTest {
    
    public ArchiveControllerTest() {
    }
    
    private AuditLogRepository auditLogRepository;
    private InteractionRepository interactionRepository;
    ArchiveController instance;
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
        // 1. Create Mock repositorys
        auditLogRepository = mock(AuditLogRepository.class);
        interactionRepository = mock(InteractionRepository.class);
        
        // 2. Inject Mock into Controller
        instance = new ArchiveController(interactionRepository, auditLogRepository);
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getArchivablePeriods method, of class ArchiveController.
     */
    @Test
    public void testGetArchivablePeriods() {
        System.out.println("getArchivablePeriods");
        ArchiveController instance = new ArchiveController();
        ResponseEntity<List<ArchivePeriodDTO>> expResult = null;
        ResponseEntity<List<ArchivePeriodDTO>> result = instance.getArchivablePeriods();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of exportAndDelete method, of class ArchiveController.
     */
    @Test
    public void testExportAndDelete() {
        System.out.println("exportAndDelete");
        String periodId = "";
        ArchiveController instance = new ArchiveController();
        ResponseEntity expResult = null;
        ResponseEntity result = instance.exportAndDelete(periodId);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of exportAllAndDelete method, of class ArchiveController.
     */
    @Test
    public void testExportAllAndDelete() {
        System.out.println("exportAllAndDelete");
        ArchiveController instance = new ArchiveController();
        ResponseEntity expResult = null;
        ResponseEntity result = instance.exportAllAndDelete();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAuditArchivablePeriods method, of class ArchiveController.
     */
    @Test
    public void testGetAuditArchivablePeriods() {
        System.out.println("getAuditArchivablePeriods");
        ArchiveController instance = new ArchiveController();
        ResponseEntity<List<ArchivePeriodDTO>> expResult = null;
        ResponseEntity<List<ArchivePeriodDTO>> result = instance.getAuditArchivablePeriods();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of exportAuditAndDelete method, of class ArchiveController.
     */
    @Test
    public void testExportAuditAndDelete() {
        System.out.println("exportAuditAndDelete");
        String periodId = "";
        ArchiveController instance = new ArchiveController();
        ResponseEntity expResult = null;
        ResponseEntity result = instance.exportAuditAndDelete(periodId);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of exportAllAuditAndDelete method, of class ArchiveController.
     */
    @Test
    public void testExportAllAuditAndDelete() {
        System.out.println("exportAllAuditAndDelete");
        ArchiveController instance = new ArchiveController();
        ResponseEntity expResult = null;
        ResponseEntity result = instance.exportAllAuditAndDelete();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
