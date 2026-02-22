/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file defines the ModuleController class, which handles CRUD operations for modules.
 * 
 * Multi-profile system:
 * - Uses role to filter modules by profile
 * - Each profile only sees its own modules
 */
package peps.peps_back.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import peps.peps_back.items.Module;
import peps.peps_back.repositories.ModuleRepository;
import peps.peps_back.repositories.UserRepository;
import peps.peps_back.repositories.UserRepository;
import peps.peps_back.items.User;
import peps.peps_back.items.Notification;
import peps.peps_back.repositories.NotificationRepository;
import peps.peps_back.services.AuditService;
import javax.persistence.OptimisticLockException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/modules")
@CrossOrigin(origins = "http://localhost:4200")
public class ModuleController {

    @Autowired
    private ModuleRepository moduleRepository;

    public ModuleController(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuditService auditService;

    /**
     * Lists modules filtered by role.
     * 
     * @param role Role to filter by (e.g., 'dauphin', 'aras'). If null, returns
     *             all.
     */
    @GetMapping
    public ResponseEntity<List<ModuleDTO>> getAllModules(@RequestParam(required = false) String role) {
        List<Module> modules;

        // If role is provided, filter by owner_role
        if (role != null && !role.isEmpty()) {
            modules = moduleRepository.findByOwnerRole(role.toLowerCase());
        } else {
            modules = moduleRepository.findAll();
        }

        List<ModuleDTO> dtos = modules.stream()
                .map(m -> new ModuleDTO(
                        m.getIdmodule(),
                        m.getNom(),
                        "",
                        m.getActif() ? "Actif" : "Inactif",
                        m.getIpAdress(),
                        new ModuleConfigDTO(
                                m.getVolume(),
                                m.getCurrentMode(),
                                m.getActif(),
                                false)))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleDTO> getModule(@PathVariable Integer id) {
        Module m = moduleRepository.findById(id).orElse(null);
        if (m == null) {
            return ResponseEntity.notFound().build();
        }

        ModuleDTO dto = new ModuleDTO(
                m.getIdmodule(),
                m.getNom(),
                "",
                m.getActif() ? "Actif" : "Inactif",
                m.getIpAdress(),
                new ModuleConfigDTO(
                        m.getVolume(),
                        m.getCurrentMode(),
                        m.getActif(),
                        false));

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateModule(@PathVariable Integer id, @RequestBody ModuleDTO dto,
            @RequestHeader(value = "X-User-Login", required = false) String login) {

        // Permission Check
        if (login != null) {
            User user = userRepository.findByLogin(login).orElse(null);
            if (user != null && !"admin".equals(user.getRole()) && "viewer".equals(user.getPermission())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Accès refusé : Vous avez la permission Viewer uniquement.");
                return ResponseEntity.status(403).body(error);
            }
        }

        Module module = moduleRepository.findById(id).orElse(null);
        if (module == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Module introuvable");
            return ResponseEntity.notFound().build();
        }

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Le nom du module est obligatoire");
            return ResponseEntity.badRequest().body(error);
        }

        if (dto.getIp() == null || dto.getIp().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "L'adresse IP est obligatoire");
            return ResponseEntity.badRequest().body(error);
        }

        if (!dto.getIp().matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Format d'adresse IP invalide");
            return ResponseEntity.badRequest().body(error);
        }

        if (dto.getConfig() == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "La configuration est obligatoire");
            return ResponseEntity.badRequest().body(error);
        }

        if (dto.getConfig().getVolume() < 0 || dto.getConfig().getVolume() > 100) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Le volume doit être entre 0 et 100");
            return ResponseEntity.badRequest().body(error);
        }

        if (!dto.getConfig().getMode().equals("Manuel") && !dto.getConfig().getMode().equals("Automatique")) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Le mode doit être 'Manuel' ou 'Automatique'");
            return ResponseEntity.badRequest().body(error);
        }

        // Capture old values for audit log and notification logic
        String oldValue = String.format(
                "{\"name\":\"%s\",\"ip\":\"%s\",\"volume\":%d,\"mode\":\"%s\",\"actif\":%b}",
                module.getNom(), module.getIpAdress(), module.getVolume(),
                module.getCurrentMode(), module.getActif());

        boolean wasActive = module.getActif();
        boolean isNowActive = dto.getConfig().isActif();

        module.setNom(dto.getName());
        module.setIpAdress(dto.getIp());
        module.setVolume(dto.getConfig().getVolume());
        module.setCurrentMode(dto.getConfig().getMode());
        module.setActif(dto.getConfig().isActif());
        module.setStatus(dto.getConfig().isActif() ? "actif" : "inactif");
        module.setLastSeen(new java.util.Date());

        moduleRepository.save(module);

        // Capture new values for audit log
        String newValue = String.format(
                "{\"name\":\"%s\",\"ip\":\"%s\",\"volume\":%d,\"mode\":\"%s\",\"actif\":%b}",
                module.getNom(), module.getIpAdress(), module.getVolume(),
                module.getCurrentMode(), module.getActif());

        try {
            moduleRepository.save(module);

            // Log update
            String userLogin = (login != null) ? login : "unknown";
            auditService.log("UPDATE", "module", module.getIdmodule(), module.getNom(),
                    module.getOwnerRole(), userLogin, oldValue, newValue, "Modification du module");

            // Notification Trigger: module offline
            if (wasActive && !isNowActive) {
                Notification notif = new Notification(
                        "MODULE_OFFLINE|" + module.getNom() + "|" + module.getIpAdress() + "|" + module.getOwnerRole(),
                        module.getOwnerRole());
                notificationRepository.save(notif);
            }

            ModuleDTO updatedDto = new ModuleDTO(
                    module.getIdmodule(),
                    module.getNom(),
                    dto.getLocation(),
                    module.getActif() ? "Actif" : "Inactif",
                    module.getIpAdress(),
                    new ModuleConfigDTO(
                            module.getVolume(),
                            module.getCurrentMode(),
                            module.getActif(),
                            dto.getConfig().isSon()));

            return ResponseEntity.ok(updatedDto);

        } catch (OptimisticLockException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Conflit de modification : le module a été modifié par un autre utilisateur.");
            return ResponseEntity.status(409).body(error);
        }
    }

    @PostMapping
    public ResponseEntity<?> createModule(@RequestBody ModuleDTO dto,
            @RequestParam(required = false) String role,
            @RequestHeader(value = "X-User-Login", required = false) String login) {

        // Permission Check
        if (login != null) {
            User user = userRepository.findByLogin(login).orElse(null);
            if (user != null && !"admin".equals(user.getRole()) && "viewer".equals(user.getPermission())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Accès refusé : Vous avez la permission Viewer uniquement.");
                return ResponseEntity.status(403).body(error);
            }
        }

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Le nom du module est obligatoire");
            return ResponseEntity.badRequest().body(error);
        }

        if (dto.getIp() == null || dto.getIp().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "L'adresse IP est obligatoire");
            return ResponseEntity.badRequest().body(error);
        }

        if (!dto.getIp().matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Format d'adresse IP invalide");
            return ResponseEntity.badRequest().body(error);
        }

        if (dto.getConfig() == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "La configuration est obligatoire");
            return ResponseEntity.badRequest().body(error);
        }

        Module module = new Module();
        module.setNom(dto.getName());
        module.setIpAdress(dto.getIp());
        module.setVolume(dto.getConfig().getVolume());
        module.setCurrentMode(dto.getConfig().getMode());
        module.setActif(dto.getConfig().isActif());
        module.setStatus(dto.getConfig().isActif() ? "actif" : "inactif");
        module.setLastSeen(new java.util.Date());

        // Set owner role for multi-profile filtering
        if (role != null && !role.isEmpty()) {
            module.setOwnerRole(role.toLowerCase());
        }

        module = moduleRepository.save(module);

        // Log creation in audit
        String newValue = String.format(
                "{\"name\":\"%s\",\"ip\":\"%s\",\"volume\":%d,\"mode\":\"%s\",\"actif\":%b}",
                module.getNom(), module.getIpAdress(), module.getVolume(),
                module.getCurrentMode(), module.getActif());

        String userLogin = (login != null) ? login : "unknown";
        if (auditService != null)
            auditService.log("CREATE", "module", module.getIdmodule(), module.getNom(),
                    module.getOwnerRole(), userLogin, null, newValue, "Création d'un module");

        ModuleDTO createdDto = new ModuleDTO(
                module.getIdmodule(),
                module.getNom(),
                dto.getLocation(),
                module.getActif() ? "Actif" : "Inactif",
                module.getIpAdress(),
                new ModuleConfigDTO(
                        module.getVolume(),
                        module.getCurrentMode(),
                        module.getActif(),
                        dto.getConfig().isSon()));

        return ResponseEntity.ok(createdDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteModule(@PathVariable Integer id,
            @RequestHeader(value = "X-User-Login", required = false) String login) {

        // Permission Check
        if (login != null) {
            User user = userRepository.findByLogin(login).orElse(null);
            if (user != null && !"admin".equals(user.getRole()) && "viewer".equals(user.getPermission())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Accès refusé : Vous avez la permission Viewer uniquement.");
                return ResponseEntity.status(403).body(error);
            }
        }

        Module module = moduleRepository.findById(id).orElse(null);
        if (module == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Module introuvable");
            return ResponseEntity.notFound().build();
        }

        // Log deletion in audit
        String oldValue = String.format(
                "{\"name\":\"%s\",\"ip\":\"%s\",\"ownerRole\":\"%s\"}",
                module.getNom(), module.getIpAdress(), module.getOwnerRole());

        String userLogin = (login != null) ? login : "unknown";
        if (auditService != null)
            auditService.log("DELETE", "module", id, module.getNom(),
                    module.getOwnerRole(), userLogin, oldValue, null, "Suppression d'un module");

        moduleRepository.delete(module);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Module supprimé avec succès");
        return ResponseEntity.ok(response);
    }
}
