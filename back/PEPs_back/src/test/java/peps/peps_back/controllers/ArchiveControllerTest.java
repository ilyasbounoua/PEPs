package peps.peps_back.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import peps.peps_back.items.AuditLog;
import peps.peps_back.items.Interaction;
import peps.peps_back.repositories.AuditLogRepository;
import peps.peps_back.repositories.InteractionRepository;

/**
 * Unit tests for ArchiveController.
 */
public class ArchiveControllerTest {

    private InteractionRepository interactionRepository;
    private AuditLogRepository auditLogRepository;
    private ArchiveController instance;

    @BeforeEach
    public void setUp() {
        interactionRepository = mock(InteractionRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);
        instance = new ArchiveController(interactionRepository, auditLogRepository);
    }

    @Test
    @DisplayName("Should return empty list when no archivable interactions exist")
    public void testGetArchivablePeriods_Empty() {
        when(interactionRepository.findByTimeLancementBefore(any(Date.class)))
            .thenReturn(Collections.emptyList());

        ResponseEntity<List<ArchivePeriodDTO>> result = instance.getArchivablePeriods();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    @DisplayName("Should generate periods when old interactions exist")
    public void testGetArchivablePeriods_WithData() {
        // Create a date older than 3 months
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -6);
        Date oldDate = cal.getTime();

        Interaction mockInteraction = new Interaction();
        mockInteraction.setTimeLancement(oldDate);

        when(interactionRepository.findByTimeLancementBefore(any(Date.class)))
            .thenReturn(List.of(mockInteraction));
        
        Long l = new Long(1);
        when(interactionRepository.countByTimeLancementBetween(any(Date.class), any(Date.class)))
            .thenReturn(l);

        ResponseEntity<List<ArchivePeriodDTO>> result = instance.getArchivablePeriods();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertFalse(result.getBody().isEmpty());
        verify(interactionRepository, atLeastOnce()).countByTimeLancementBetween(any(), any());
    }

    @Test
    @DisplayName("Should export and delete interactions for a valid period")
    public void testExportAndDelete() {
        String periodId = "2023-01"; // Format expected by parsePeriodId: yyyy-MM
        Interaction mockInteraction = new Interaction();
        mockInteraction.setTimeLancement(new Date());
        
        when(interactionRepository.findByTimeLancementBetween(any(Date.class), any(Date.class)))
            .thenReturn(List.of(mockInteraction));

        ResponseEntity<byte[]> result = instance.exportAndDelete(periodId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        // Verify deletion logic was triggered
        verify(interactionRepository).deleteByTimeLancementBetween(any(Date.class), any(Date.class));
    }

    @Test
    @DisplayName("Should return 404 if no interactions found for period export")
    public void testExportAndDelete_NotFound() {
        String periodId = "2020-01";
        when(interactionRepository.findByTimeLancementBetween(any(Date.class), any(Date.class)))
            .thenReturn(Collections.emptyList());

        ResponseEntity<byte[]> result = instance.exportAndDelete(periodId);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    @DisplayName("Should export and delete all audit logs older than cutoff")
    public void testExportAllAuditAndDelete() {
        AuditLog log = new AuditLog();
        log.setTimestamp(new Date());
        log.setAction("LOGIN");
        log.setEntityType("USER");

        when(auditLogRepository.findByTimestampBefore(any(Date.class)))
            .thenReturn(List.of(log));

        ResponseEntity<byte[]> result = instance.exportAllAuditAndDelete();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        // Verify repository interaction
        verify(auditLogRepository).deleteByTimestampBetween(any(Date.class), any(Date.class));
    }

    @Test
    @DisplayName("Should handle errors during audit export gracefully")
    public void testExportAuditAndDelete_Error() {
        // Force an exception (e.g., malformed period ID)
        ResponseEntity<byte[]> result = instance.exportAuditAndDelete("invalid-id");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }
}