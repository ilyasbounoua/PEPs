/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file defines the InteractionController class, which handles requests for retrieving and creating interactions.
 * * Multi-profile system:
 * - Uses role to filter interactions by profile
 * - Each profile only sees its own interactions
 * * Archive system:
 * - Only shows interactions from the last 3 months
 * - Older interactions are managed via the Archive section
 */
package peps.peps_back.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import peps.peps_back.items.Interaction;
import peps.peps_back.items.Module;
import peps.peps_back.items.Sound;
import peps.peps_back.items.User;
import peps.peps_back.repositories.InteractionRepository;
import peps.peps_back.repositories.ModuleRepository;
import peps.peps_back.repositories.SoundRepository;
import peps.peps_back.repositories.UserRepository;
import peps.peps_back.services.AuditService;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/interactions")
@CrossOrigin(origins = "http://localhost:4200")
public class InteractionController {

    @Autowired
    private InteractionRepository interactionRepository;

    // Ajout des repositories nécessaires pour le POST
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private SoundRepository soundRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private peps.peps_back.services.AudioJobPublisher audioJobPublisher;

    public InteractionController(InteractionRepository interactionRepository) {
        this.interactionRepository = interactionRepository;
    }
    
    /**
     * Lists interactions filtered by role.
     * Only returns interactions from the last 3 months.
     * Older interactions are available via the Archive section.
     * * @param role Role to filter by (e.g., 'dauphin', 'aras'). If null, returns all.
     */
    @GetMapping
    public ResponseEntity<List<InteractionDTO>> getAllInteractions(@RequestParam(required = false) String role) {
        List<Interaction> interactions;

        // Calculate the cutoff date (3 months ago)
        Calendar cutoff = Calendar.getInstance();
        cutoff.add(Calendar.MONTH, -3);
        Date cutoffDate = cutoff.getTime();

        // If role is provided, filter by owner_role AND date
        if (role != null && !role.isEmpty()) {
            interactions = interactionRepository.findByOwnerRoleAndTimeLancementAfter(role.toLowerCase(), cutoffDate);
        } else {
            interactions = interactionRepository.findByTimeLancementAfter(cutoffDate);
        }

        List<InteractionDTO> dtos = interactions.stream()
                .map(i -> new InteractionDTO(
                        i.getIdinteraction(),
                        i.getTimeLancement(),
                        i.getIdmodule() != null ? i.getIdmodule().getNom() : "Unknown",
                        i.getTypeinteraction()))
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Crée une nouvelle interaction.
     */
    @PostMapping
    public ResponseEntity<?> createInteraction(
            @RequestBody InteractionRequestDTO dto,
            @RequestParam(required = false) String role,
            @RequestHeader(value = "X-User-Login", required = false) String login) {

        // 1. Vérification des permissions
        if (login != null) {
            User user = userRepository.findByLogin(login).orElse(null);
            if (user != null && !"admin".equals(user.getRole()) && "viewer".equals(user.getPermission())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Accès refusé : Vous avez la permission Viewer uniquement.");
                return ResponseEntity.status(403).body(error);
            }
        }

        // 2. Validation des champs obligatoires
        if (dto.getTypeInteraction() == null || dto.getTypeInteraction().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Le type d'interaction est obligatoire");
            return ResponseEntity.badRequest().body(error);
        }

        // Récupération et validation du Module associé
        Module module = null;
        if (dto.getIdmodule() != null) {
            module = moduleRepository.findById(dto.getIdmodule()).orElse(null);
            if (module == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Module introuvable");
                return ResponseEntity.badRequest().body(error);
            }
        }

        // Récupération et validation du Son associé (Optionnel selon ta logique métier)
        Sound sound = null;
        if (dto.getIdsound() != null) {
            sound = soundRepository.findById(dto.getIdsound()).orElse(null);
            if (sound == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Son introuvable");
                return ResponseEntity.badRequest().body(error);
            }
        }

        // 3. Création de l'entité
        Interaction interaction = new Interaction();
        interaction.setTypeinteraction(dto.getTypeInteraction());
        interaction.setIdmodule(module);
        interaction.setIdsound(sound);
        interaction.setTimeLancement(new java.util.Date()); // Ou via NOW() de la BDD

        // Gestion du rôle pour le multi-profil
        if (role != null && !role.isEmpty()) {
            interaction.setOwnerRole(role.toLowerCase()); 
        }

        interaction = interactionRepository.save(interaction);

        // 4. Audit Log
        String newValue = String.format(
                "{\"type\":\"%s\",\"idmodule\":%d,\"idsound\":%d}",
                interaction.getTypeinteraction(),
                module != null ? module.getIdmodule() : null,
                sound != null ? sound.getIdsound() : null);

        String userLogin = (login != null) ? login : "unknown";
        if (auditService != null) {
            auditService.log("CREATE", "interaction", interaction.getIdinteraction(), 
                "Interaction " + interaction.getTypeinteraction(),
                interaction.getOwnerRole(), userLogin, null, newValue, "Création d'une interaction");
        }

        // 4.5. Trigger Pipeline 2 (Retrieval) if a sound is associated
        // This ensures the audio is pre-processed and ready in Redis for the ESP32.
        if (sound != null && audioJobPublisher != null) {
            Map<String, String> retrievalPayload = new HashMap<>();
            String retrievalJobId = java.util.UUID.randomUUID().toString();
            retrievalPayload.put("jobId", retrievalJobId);
            retrievalPayload.put("soundId", sound.getIdsound().toString());
            
            try {
                audioJobPublisher.publishRetrievalJob(retrievalPayload);
                System.out.println("Triggered retrieval job " + retrievalJobId + " for interaction " + interaction.getIdinteraction());
            } catch (Exception e) {
                System.err.println("Failed to trigger retrieval job: " + e.getMessage());
            }
        }

        // 5. Retour du DTO
        InteractionDTO createdDto = new InteractionDTO(
                interaction.getIdinteraction(),
                interaction.getTimeLancement(),
                module != null ? module.getNom() : "Unknown",
                interaction.getTypeinteraction()
        );

        return ResponseEntity.ok(createdDto);
    }
}