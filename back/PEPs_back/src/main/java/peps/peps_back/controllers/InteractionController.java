/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file defines the InteractionController class, which handles requests for retrieving all interactions.
 * 
 * Multi-profile system:
 * - Uses role to filter interactions by profile
 * - Each profile only sees its own interactions
 * 
 * Archive system:
 * - Only shows interactions from the last 3 months
 * - Older interactions are managed via the Archive section
 */
package peps.peps_back.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import peps.peps_back.items.Interaction;
import peps.peps_back.repositories.InteractionRepository;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/interactions")
@CrossOrigin(origins = "http://localhost:4200")
public class InteractionController {

    @Autowired
    private InteractionRepository interactionRepository;

    public InteractionController(InteractionRepository interactionRepository)
    {
        this.interactionRepository=interactionRepository;
    }
    
    /**
     * Lists interactions filtered by role.
     * Only returns interactions from the last 3 months.
     * Older interactions are available via the Archive section.
     * 
     * @param role Role to filter by (e.g., 'dauphin', 'aras'). If null, returns
     *             all.
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
}
