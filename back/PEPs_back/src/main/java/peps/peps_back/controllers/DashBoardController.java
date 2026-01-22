/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file defines the DashBoardController class, which handles requests for dashboard statistics.
 * 
 * Multi-profile system:
 * - Admin (role=null) sees stats for ALL profiles
 * - Regular users: pass role to filter by profile
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
import peps.peps_back.repositories.InteractionRepository;
import peps.peps_back.repositories.ModuleRepository;

import java.text.SimpleDateFormat;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class DashBoardController {

    @Autowired
    private InteractionRepository interactionRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    /**
     * Returns dashboard statistics.
     * 
     * @param role Role to filter by (e.g., 'dauphin', 'aras'). If null, returns
     *             stats for ALL.
     * @author Anas EL HOUDI
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> dashboard(@RequestParam(required = false) String role) {
        List<Interaction> interactions;
        List<Module> modules;

        // If role is provided, filter by owner_role
        if (role != null && !role.isEmpty()) {
            interactions = interactionRepository.findByOwnerRole(role.toLowerCase());
            modules = moduleRepository.findByOwnerRole(role.toLowerCase());
        } else {
            // No role (admin view): get all
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
