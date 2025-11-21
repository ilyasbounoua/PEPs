/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package peps.peps_back.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import peps.peps_back.items.Interaction;
import peps.peps_back.items.Module;
import peps.peps_back.repositories.InteractionRepository;
import peps.peps_back.repositories.ModuleRepository;

import java.text.SimpleDateFormat;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
/**
 *
 * @author Clément
 */
public class DashBoardController {

    @Autowired
    private InteractionRepository interactionRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> dashboard() {
        long totalInteractions = interactionRepository.count();

        List<Module> modules = moduleRepository.findAll();
        long activeModules = modules.stream()
                .filter(Module::getActif)
                .count();

        String lastInteraction = "Aucune interaction";
        List<Interaction> interactions = interactionRepository.findAll();
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
                lastInteraction
        );

        return ResponseEntity.ok(stats);
    }

}
