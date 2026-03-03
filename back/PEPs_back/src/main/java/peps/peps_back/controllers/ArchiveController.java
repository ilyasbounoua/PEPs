/**
 * @author Anas EL HOUDI
 * @description REST controller for archive operations.
 * Allows administrators to view, export, and delete old interaction logs
 * and audit logs grouped by 3-month periods.
 * 
 * Features:
 * - GET /archive/periods: Returns available interaction archive periods
 * - POST /archive/export: Exports interactions for a period
 * - POST /archive/export-all: Exports all interaction periods
 * - GET /archive/audit-periods: Returns available audit log archive periods
 * - POST /archive/audit-export: Exports audit logs for a period
 * - POST /archive/audit-export-all: Exports all audit log periods
 */
package peps.peps_back.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import peps.peps_back.items.AuditLog;
import peps.peps_back.items.Interaction;
import peps.peps_back.repositories.AuditLogRepository;
import peps.peps_back.repositories.InteractionRepository;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/archive")
@CrossOrigin(origins = "http://localhost:4200")
public class ArchiveController {

    @Autowired
    private InteractionRepository interactionRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String[] MONTH_NAMES_FR = {
            "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
            "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
    };

    public ArchiveController(InteractionRepository interactionRepository, AuditLogRepository auditLogRepository)
    {
        this.interactionRepository = interactionRepository;
        this.auditLogRepository = auditLogRepository;
    }
    
    /* ===================== */
    /* INTERACTION ARCHIVE */
    /* ===================== */

    /**
     * Returns available archive periods (3-month windows older than 3 months).
     * Periods are calculated using rolling 3-month windows from the oldest data.
     */
    @GetMapping("/periods")
    public ResponseEntity<List<ArchivePeriodDTO>> getArchivablePeriods() {
        Date cutoffDate = getCutoffDate();

        List<Interaction> oldInteractions = interactionRepository.findByTimeLancementBefore(cutoffDate);

        if (oldInteractions.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        Date oldestDate = oldInteractions.stream()
                .map(Interaction::getTimeLancement)
                .min(Date::compareTo)
                .orElse(cutoffDate);

        List<ArchivePeriodDTO> periods = generatePeriods(oldestDate, cutoffDate,
                (start, end) -> (int) interactionRepository.countByTimeLancementBetween(start, end));

        return ResponseEntity.ok(periods);
    }

    /**
     * Exports interactions for a specific period as JSON and deletes them from DB.
     */
    @PostMapping("/export")
    public ResponseEntity<byte[]> exportAndDelete(@RequestParam String periodId) {
        try {
            DateRange range = parsePeriodId(periodId);

            List<Interaction> interactions = interactionRepository.findByTimeLancementBetween(range.start, range.end);

            if (interactions.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<InteractionExportDTO> exportData = interactions.stream()
                    .map(i -> new InteractionExportDTO(
                            DATE_FORMAT.format(i.getTimeLancement()),
                            i.getIdmodule() != null ? i.getIdmodule().getNom() : "Unknown",
                            i.getTypeinteraction()))
                    .collect(Collectors.toList());

            ObjectMapper mapper = new ObjectMapper();
            byte[] jsonBytes = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(exportData);

            interactionRepository.deleteByTimeLancementBetween(range.start, range.end);

            String filename = String.format("interactions_%s.json", periodId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonBytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Exports ALL interaction archive periods as a single JSON file and deletes
     * them.
     */
    @PostMapping("/export-all")
    public ResponseEntity<byte[]> exportAllAndDelete() {
        try {
            Date cutoffDate = getCutoffDate();

            List<Interaction> oldInteractions = interactionRepository.findByTimeLancementBefore(cutoffDate);

            if (oldInteractions.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<InteractionExportDTO> exportData = oldInteractions.stream()
                    .sorted((a, b) -> a.getTimeLancement().compareTo(b.getTimeLancement()))
                    .map(i -> new InteractionExportDTO(
                            DATE_FORMAT.format(i.getTimeLancement()),
                            i.getIdmodule() != null ? i.getIdmodule().getNom() : "Unknown",
                            i.getTypeinteraction()))
                    .collect(Collectors.toList());

            ObjectMapper mapper = new ObjectMapper();
            byte[] jsonBytes = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(exportData);

            Calendar startOfTime = Calendar.getInstance();
            startOfTime.set(1970, 0, 1, 0, 0, 0);
            interactionRepository.deleteByTimeLancementBetween(startOfTime.getTime(), cutoffDate);

            String filename = String.format("interactions_archive_%s.json",
                    new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonBytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /* ===================== */
    /* AUDIT LOG ARCHIVE */
    /* ===================== */

    /**
     * Returns available audit log archive periods (3-month windows older than 3
     * months).
     */
    @GetMapping("/audit-periods")
    public ResponseEntity<List<ArchivePeriodDTO>> getAuditArchivablePeriods() {
        Date cutoffDate = getCutoffDate();

        List<AuditLog> oldLogs = auditLogRepository.findByTimestampBefore(cutoffDate);

        if (oldLogs.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        Date oldestDate = oldLogs.stream()
                .map(AuditLog::getTimestamp)
                .min(Date::compareTo)
                .orElse(cutoffDate);

        List<ArchivePeriodDTO> periods = generatePeriods(oldestDate, cutoffDate,
                (start, end) -> (int) auditLogRepository.countByTimestampBetween(start, end));

        return ResponseEntity.ok(periods);
    }

    /**
     * Exports audit logs for a specific period as JSON and deletes them from DB.
     */
    @PostMapping("/audit-export")
    public ResponseEntity<byte[]> exportAuditAndDelete(@RequestParam String periodId) {
        try {
            DateRange range = parsePeriodId(periodId);

            List<AuditLog> logs = auditLogRepository.findByDateRange(range.start, range.end);

            if (logs.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<AuditLogExportDTO> exportData = logs.stream()
                    .map(this::convertToExportDTO)
                    .collect(Collectors.toList());

            ObjectMapper mapper = new ObjectMapper();
            byte[] jsonBytes = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(exportData);

            auditLogRepository.deleteByTimestampBetween(range.start, range.end);

            String filename = String.format("audit_logs_%s.json", periodId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonBytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Exports ALL audit log archive periods as a single JSON file and deletes them.
     */
    @PostMapping("/audit-export-all")
    public ResponseEntity<byte[]> exportAllAuditAndDelete() {
        try {
            Date cutoffDate = getCutoffDate();

            List<AuditLog> oldLogs = auditLogRepository.findByTimestampBefore(cutoffDate);

            if (oldLogs.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<AuditLogExportDTO> exportData = oldLogs.stream()
                    .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                    .map(this::convertToExportDTO)
                    .collect(Collectors.toList());

            ObjectMapper mapper = new ObjectMapper();
            byte[] jsonBytes = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(exportData);

            Calendar startOfTime = Calendar.getInstance();
            startOfTime.set(1970, 0, 1, 0, 0, 0);
            auditLogRepository.deleteByTimestampBetween(startOfTime.getTime(), cutoffDate);

            String filename = String.format("audit_logs_archive_%s.json",
                    new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonBytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /* ===================== */
    /* HELPER METHODS */
    /* ===================== */

    /**
     * Converts an AuditLog entity to an export DTO with complete text (no
     * ellipsis).
     */
    private AuditLogExportDTO convertToExportDTO(AuditLog log) {
        String entity = formatEntityInfo(log);
        return new AuditLogExportDTO(
                log.getAction(),
                entity,
                log.getUserLogin(),
                DATE_FORMAT.format(log.getTimestamp()),
                log.getDetails() != null ? log.getDetails() : "");
    }

    /**
     * Formats entity information as shown in the Journal d'Audit section.
     */
    private String formatEntityInfo(AuditLog log) {
        StringBuilder sb = new StringBuilder();
        sb.append(capitalizeFirst(log.getEntityType()));

        if (log.getEntityName() != null && !log.getEntityName().isEmpty()) {
            sb.append(": ").append(log.getEntityName());
        }

        if (log.getEntityRole() != null && !log.getEntityRole().isEmpty()) {
            sb.append(" (").append(log.getEntityRole()).append(")");
        }

        return sb.toString();
    }

    /**
     * Capitalizes the first letter of a string.
     */
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty())
            return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Gets the cutoff date (3 months ago from today at start of month).
     */
    private Date getCutoffDate() {
        Calendar cutoff = Calendar.getInstance();
        cutoff.add(Calendar.MONTH, -3);
        cutoff.set(Calendar.DAY_OF_MONTH, 1);
        cutoff.set(Calendar.HOUR_OF_DAY, 0);
        cutoff.set(Calendar.MINUTE, 0);
        cutoff.set(Calendar.SECOND, 0);
        cutoff.set(Calendar.MILLISECOND, 0);
        return cutoff.getTime();
    }

    /**
     * Parses a period ID (e.g., "2025-07") into a date range.
     */
    private DateRange parsePeriodId(String periodId) {
        String[] parts = periodId.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]) - 1;

        Calendar periodStart = Calendar.getInstance();
        periodStart.set(year, month, 1, 0, 0, 0);
        periodStart.set(Calendar.MILLISECOND, 0);

        Calendar periodEnd = (Calendar) periodStart.clone();
        periodEnd.add(Calendar.MONTH, 3);
        periodEnd.add(Calendar.MILLISECOND, -1);

        return new DateRange(periodStart.getTime(), periodEnd.getTime());
    }

    /**
     * Generates 3-month periods from oldest date to cutoff.
     */
    private List<ArchivePeriodDTO> generatePeriods(Date oldestDate, Date cutoffDate,
            java.util.function.BiFunction<Date, Date, Integer> countFunction) {
        List<ArchivePeriodDTO> periods = new ArrayList<>();
        Calendar periodStart = Calendar.getInstance();
        periodStart.setTime(oldestDate);
        periodStart.set(Calendar.DAY_OF_MONTH, 1);
        periodStart.set(Calendar.HOUR_OF_DAY, 0);
        periodStart.set(Calendar.MINUTE, 0);
        periodStart.set(Calendar.SECOND, 0);
        periodStart.set(Calendar.MILLISECOND, 0);

        while (periodStart.getTime().before(cutoffDate)) {
            Calendar periodEnd = (Calendar) periodStart.clone();
            periodEnd.add(Calendar.MONTH, 3);
            periodEnd.add(Calendar.MILLISECOND, -1);

            if (periodEnd.getTime().after(cutoffDate)) {
                periodEnd.setTime(cutoffDate);
                periodEnd.add(Calendar.MILLISECOND, -1);
            }

            Date startDate = periodStart.getTime();
            Date endDate = periodEnd.getTime();

            int count = countFunction.apply(startDate, endDate);

            if (count > 0) {
                String periodId = String.format("%d-%02d",
                        periodStart.get(Calendar.YEAR),
                        periodStart.get(Calendar.MONTH) + 1);

                String periodLabel = String.format("%s %d - %s %d",
                        MONTH_NAMES_FR[periodStart.get(Calendar.MONTH)],
                        periodStart.get(Calendar.YEAR),
                        MONTH_NAMES_FR[periodEnd.get(Calendar.MONTH)],
                        periodEnd.get(Calendar.YEAR));

                periods.add(new ArchivePeriodDTO(periodId, periodLabel, startDate, endDate, count));
            }

            periodStart.add(Calendar.MONTH, 3);
        }

        return periods;
    }

    /**
     * Helper class for date ranges.
     */
    private static class DateRange {
        final Date start;
        final Date end;

        DateRange(Date start, Date end) {
            this.start = start;
            this.end = end;
        }
    }
}
