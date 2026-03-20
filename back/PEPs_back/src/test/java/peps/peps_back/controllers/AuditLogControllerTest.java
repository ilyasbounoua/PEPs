package peps.peps_back.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import peps.peps_back.items.AuditLog;
import peps.peps_back.repositories.AuditLogRepository;

/**
 * Tests unitaires pour AuditLogController inspirés de ArchiveControllerTest.
 */
public class AuditLogControllerTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogController auditLogController;

    @BeforeEach
    public void setUp() {
        // Initialise les mocks et l'instance du contrôleur
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should return all recent logs (3 last months)")
    public void testGetAllLogs() {
        // Préparation des données factices
        AuditLog log = new AuditLog();
        log.setId(1);
        log.setAction("UPDATE");
        log.setTimestamp(new Date());

        when(auditLogRepository.findByTimestampAfterOrderByTimestampDesc(any(Date.class)))
            .thenReturn(List.of(log));

        // Exécution
        ResponseEntity<List<Map<String, Object>>> response = auditLogController.getAllLogs();

        // Vérifications
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("UPDATE", response.getBody().get(0).get("action"));
        verify(auditLogRepository).findByTimestampAfterOrderByTimestampDesc(any(Date.class));
    }

    @Test
    @DisplayName("Should return logs filtered by entity type")
    public void testGetLogsByEntity() {
        String entityType = "user";
        AuditLog log = new AuditLog();
        log.setEntityType(entityType);
        log.setTimestamp(new Date());

        when(auditLogRepository.findByEntityTypeAndTimestampAfterOrderByTimestampDesc(eq(entityType), any(Date.class)))
            .thenReturn(List.of(log));

        // Exécution
        ResponseEntity<List<Map<String, Object>>> response = auditLogController.getLogsByEntity(entityType);

        // Vérifications
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        assertEquals(entityType, response.getBody().get(0).get("entityType"));
        verify(auditLogRepository).findByEntityTypeAndTimestampAfterOrderByTimestampDesc(eq(entityType), any(Date.class));
    }

    @Test
    @DisplayName("Should return logs for a specific user login")
    public void testGetLogsByUser() {
        String userLogin = "admin_test";
        AuditLog log = new AuditLog();
        log.setUserLogin(userLogin);
        log.setTimestamp(new Date());

        when(auditLogRepository.findByUserLoginOrderByTimestampDesc(userLogin))
            .thenReturn(List.of(log));

        // Exécution
        ResponseEntity<List<Map<String, Object>>> response = auditLogController.getLogsByUser(userLogin);

        // Vérifications
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(userLogin, response.getBody().get(0).get("userLogin"));
        verify(auditLogRepository).findByUserLoginOrderByTimestampDesc(userLogin);
    }

    @Test
    @DisplayName("Should return empty list when no logs are found")
    public void testGetAllLogs_Empty() {
        when(auditLogRepository.findByTimestampAfterOrderByTimestampDesc(any(Date.class)))
            .thenReturn(Collections.emptyList());

        ResponseEntity<List<Map<String, Object>>> response = auditLogController.getAllLogs();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("Should correctly map AuditLog entity fields to DTO map")
    public void testToDTOMapping() {
        // Test indirect de la méthode privée toDTO via l'appel public
        AuditLog log = new AuditLog();
        log.setId(99);
        log.setAction("DELETE");
        log.setDetails("Test details");
        log.setTimestamp(new Date(123456789L));

        when(auditLogRepository.findByUserLoginOrderByTimestampDesc(anyString()))
            .thenReturn(List.of(log));

        ResponseEntity<List<Map<String, Object>>> response = auditLogController.getLogsByUser("any");
        Map<String, Object> dto = response.getBody().get(0);

        assertEquals(99, dto.get("id"));
        assertEquals("DELETE", dto.get("action"));
        assertEquals("Test details", dto.get("details"));
        assertEquals(123456789L, dto.get("timestamp"));
    }
}