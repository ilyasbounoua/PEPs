/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file defines the DashBoardController class, which handles requests for dashboard statistics.
 * 
 * Multi-profile system:
 * - Admin (ownerId=null) sees stats for ALL users
 * - Regular users see only their own stats
 */
package peps.peps_back.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import peps.peps_back.items.Interaction;
import peps.peps_back.items.Module;
import peps.peps_back.items.User;
import peps.peps_back.repositories.InteractionRepository;
import peps.peps_back.repositories.ModuleRepository;
import peps.peps_back.repositories.UserRepository;

import java.text.SimpleDateFormat;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class DashBoardController {

    @Autowired
    private InteractionRepository interactionRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Returns dashboard statistics.
     * 
     * @param ownerId If provided, filters stats by owner (for regular users).
     *                If null or admin, returns stats for ALL users.
     * @author Anas EL HOUDI
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> dashboard(@RequestParam(required = false) Integer ownerId) {
        List<Interaction> interactions;
        List<Module> modules;

        // If ownerId is provided, filter by owner (for regular users)
        if (ownerId != null) {
            User owner = userRepository.findById(ownerId).orElse(null);
            if (owner != null && !"admin".equals(owner.getRole())) {
                // Regular user: filter by owner
                interactions = interactionRepository.findByOwner(owner);
                modules = moduleRepository.findByOwner(owner);
            } else {
                // Admin or unknown: get all
                interactions = interactionRepository.findAll();
                modules = moduleRepository.findAll();
            }
        } else {
            // No ownerId (admin view): get all
            interactions = interactionRepository.findAll();
            modules = moduleRepository.findAll();
        }

        long totalInteractions = interactions.size();
        long activeModules = modules.stream()
                .filter(Module::getActif)
                .count();

        String lastInteraction = "No interactions";
        if (!interactions.isEmpty()) {
            Interaction latest = interactions.stream()
                    .max((i1, i2) -> i1.getTimeLancement().compareTo(i2.getTimeLancement()))
                    .orElse(null);

            if (latest != null && latest.getTimeLancement() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                lastInteraction = sdf.format(latest.getTimeLancement());
            }
        }

        DashboardStats stats = new DashboardStats(
                (int) totalInteractions,
                (int) activeModules,
                lastInteraction);

        return ResponseEntity.ok(stats);
    }

}
