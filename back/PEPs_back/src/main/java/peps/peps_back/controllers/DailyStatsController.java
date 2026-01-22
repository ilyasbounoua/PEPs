/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file defines the DailyStatsController class, which handles requests for daily interaction statistics.
 * 
 * Multi-profile system:
 * - Uses role to filter daily stats by profile
 */
package peps.peps_back.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import peps.peps_back.items.Interaction;
import peps.peps_back.repositories.InteractionRepository;

import java.util.*;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@RestController
@RequestMapping("/daily-stats")
@CrossOrigin(origins = "http://localhost:4200")
public class DailyStatsController {

    @Autowired
    private InteractionRepository interactionRepository;

    /**
     * Returns daily stats.
     * 
     * @param role Role to filter by (e.g., 'dauphin', 'aras'). If null, returns
     *             all.
     * @author Anas EL HOUDI
     */
    @GetMapping
    public ResponseEntity<List<DailyDataDTO>> getDailyStats(@RequestParam(required = false) String role) {
        List<Interaction> interactions;

        // If role is provided, filter by owner_role
        if (role != null && !role.isEmpty()) {
            interactions = interactionRepository.findByOwnerRole(role.toLowerCase());
        } else {
            interactions = interactionRepository.findAll();
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();

        List<Interaction> todayInteractions = interactions.stream()
                .filter(i -> i.getTimeLancement().compareTo(startOfDay) >= 0)
                .collect(Collectors.toList());

        Map<String, Integer> hourCounts = new HashMap<>();
        for (int hour = 8; hour <= 18; hour += 2) {
            hourCounts.put(hour + "h", 0);
        }

        SimpleDateFormat hourFormat = new SimpleDateFormat("H");
        for (Interaction interaction : todayInteractions) {
            int hour = Integer.parseInt(hourFormat.format(interaction.getTimeLancement()));
            for (int h = 8; h <= 18; h += 2) {
                if (hour >= h && hour < h + 2) {
                    String key = h + "h";
                    hourCounts.put(key, hourCounts.get(key) + 1);
                    break;
                }
            }
        }

        List<DailyDataDTO> result = new ArrayList<>();
        for (int hour = 8; hour <= 18; hour += 2) {
            String key = hour + "h";
            result.add(new DailyDataDTO(key, hourCounts.get(key)));
        }

        return ResponseEntity.ok(result);
    }
}
