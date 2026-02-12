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
    
    public DailyStatsController(InteractionRepository interactionRepository)
    {
        this.interactionRepository = interactionRepository;
    }

    /**
     * Returns daily stats.
     * 
     * @param role Role to filter by (e.g., 'dauphin', 'aras'). If null, returns
     *             all.
     * @author Anas EL HOUDI
     */
    /**
     * Returns daily stats.
     * 
     * @param role      Role to filter by (e.g., 'dauphin', 'aras'). If null,
     *                  returns
     *                  all.
     * @param startDate Optional start date (ISO format).
     * @param endDate   Optional end date (ISO format).
     * @author Anas EL HOUDI
     */
    @GetMapping
    public ResponseEntity<List<DailyDataDTO>> getDailyStats(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<Interaction> interactions;

        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            java.util.Date start;
            java.util.Date end;

            if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
                start = isoFormat.parse(startDate);
                end = isoFormat.parse(endDate);
            } else {
                // Default to Today if no dates provided
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                start = cal.getTime();

                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                end = cal.getTime();
            }

            // Filter by role and date
            if (role != null && !role.isEmpty()) {
                interactions = interactionRepository.findByOwnerRoleAndTimeLancementBetween(role.toLowerCase(), start,
                        end);
            } else {
                interactions = interactionRepository.findByTimeLancementBetween(start, end);
            }

            long durationInMillis = end.getTime() - start.getTime();
            boolean isMultiDay = durationInMillis > (24 * 60 * 60 * 1000) + 1000; // Add buffer

            Map<String, Integer> counts = new LinkedHashMap<>();
            List<DailyDataDTO> result = new ArrayList<>();

            if (!isMultiDay) {
                // Hourly grouping (1-hour resolution)
                // Range: 8h to 20h
                for (int hour = 8; hour <= 20; hour++) {
                    counts.put(hour + "h", 0);
                }

                SimpleDateFormat hourFormat = new SimpleDateFormat("H");
                for (Interaction interaction : interactions) {
                    int hour = Integer.parseInt(hourFormat.format(interaction.getTimeLancement()));
                    // Check if hour is within our tracked range
                    if (hour >= 8 && hour <= 20) {
                        String key = hour + "h";
                        counts.put(key, counts.get(key) + 1);
                    }
                }

                for (int hour = 8; hour <= 20; hour++) {
                    String key = hour + "h";
                    result.add(new DailyDataDTO(key, counts.get(key)));
                }

            } else {
                // Daily grouping
                SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");

                // Initialize map with all days in range (optional, for gaps) or just raw data
                // For simplicity, we'll just group existing data first.
                // Better UX: Fill gaps with 0.

                Calendar current = Calendar.getInstance();
                current.setTime(start);
                while (!current.getTime().after(end)) {
                    counts.put(dayFormat.format(current.getTime()), 0);
                    current.add(Calendar.DATE, 1);
                }

                for (Interaction interaction : interactions) {
                    String dayKey = dayFormat.format(interaction.getTimeLancement());
                    if (counts.containsKey(dayKey)) {
                        counts.put(dayKey, counts.get(dayKey) + 1);
                    }
                }

                for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                    result.add(new DailyDataDTO(entry.getKey(), entry.getValue()));
                }
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
