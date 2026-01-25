package peps.peps_back.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import peps.peps_back.items.AuditLog;
import peps.peps_back.repositories.AuditLogRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contrôleur pour accéder au journal d'audit.
 * Réservé aux administrateurs.
 * 
 * @author Anas EL HOUDI
 */
@RestController
@RequestMapping("/audit-logs")
@CrossOrigin(origins = "*")
public class AuditLogController {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Récupère tous les logs d'audit (plus récents en premier).
     * 
     * @return Liste des entrées d'audit
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllLogs() {
        List<AuditLog> logs = auditLogRepository.findAllByOrderByTimestampDesc();

        List<Map<String, Object>> dtos = logs.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Récupère les logs pour un type d'entité spécifique.
     * 
     * @param entityType Le type d'entité (module, sound, user)
     * @return Liste des entrées d'audit filtrées
     */
    @GetMapping("/by-entity/{entityType}")
    public ResponseEntity<List<Map<String, Object>>> getLogsByEntity(@PathVariable String entityType) {
        List<AuditLog> logs = auditLogRepository.findByEntityTypeOrderByTimestampDesc(entityType);

        List<Map<String, Object>> dtos = logs.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Récupère les logs d'un utilisateur spécifique.
     * 
     * @param userLogin Le login de l'utilisateur
     * @return Liste des entrées d'audit filtrées
     */
    @GetMapping("/by-user/{userLogin}")
    public ResponseEntity<List<Map<String, Object>>> getLogsByUser(@PathVariable String userLogin) {
        List<AuditLog> logs = auditLogRepository.findByUserLoginOrderByTimestampDesc(userLogin);

        List<Map<String, Object>> dtos = logs.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Convertit une entité AuditLog en DTO pour le frontend.
     */
    private Map<String, Object> toDTO(AuditLog log) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", log.getId());
        dto.put("action", log.getAction());
        dto.put("entityType", log.getEntityType());
        dto.put("entityId", log.getEntityId());
        dto.put("entityName", log.getEntityName());
        dto.put("entityRole", log.getEntityRole());
        dto.put("userLogin", log.getUserLogin());
        dto.put("timestamp", log.getTimestamp() != null ? log.getTimestamp().getTime() : null);
        dto.put("oldValue", log.getOldValue());
        dto.put("newValue", log.getNewValue());
        dto.put("details", log.getDetails());
        return dto;
    }
}
