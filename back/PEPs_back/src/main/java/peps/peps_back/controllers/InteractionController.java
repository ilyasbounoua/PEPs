/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file defines the InteractionController class, which handles requests for retrieving all interactions.
 * 
 * Multi-profile system:
 * - Uses ownerId to filter interactions by user
 * - Each user only sees their own interactions
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
import peps.peps_back.items.User;
import peps.peps_back.repositories.InteractionRepository;
import peps.peps_back.repositories.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/interactions")
@CrossOrigin(origins = "http://localhost:4200")
public class InteractionController {

    @Autowired
    private InteractionRepository interactionRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Lists a user's interactions.
     * 
     * @param ownerId ID of the logged-in user (multi-profile filtering)
     */
    @GetMapping
    public ResponseEntity<List<InteractionDTO>> getAllInteractions(@RequestParam(required = false) Integer ownerId) {
        List<Interaction> interactions;

        // If ownerId is provided, filter by owner
        if (ownerId != null) {
            User owner = userRepository.findById(ownerId).orElse(null);
            if (owner != null) {
                interactions = interactionRepository.findByOwner(owner);
            } else {
                interactions = interactionRepository.findAll();
            }
        } else {
            interactions = interactionRepository.findAll();
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
