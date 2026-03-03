package peps.peps_back.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import peps.peps_back.items.Notification;
import peps.peps_back.items.User;
import peps.peps_back.repositories.NotificationRepository;
import peps.peps_back.repositories.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private String getUserRole(String login) {
        if (login == null)
            return null;
        Optional<User> userOpt = userRepository.findByLogin(login);
        if (userOpt.isPresent()) {
            return userOpt.get().getRole();
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<?> getAllNotifications(@RequestHeader(value = "X-User-Login") String login) {
        String role = getUserRole(login);
        if (role == null) {
            return ResponseEntity.status(401).body("Utilisateur non trouvé");
        }

        if (!"admin".equals(role)) {
            return ResponseEntity.status(403).body("Accès refusé");
        }

        List<Notification> notifs = notificationRepository.findAllByOrderByTimestampDesc();
        return ResponseEntity.ok(notifs);
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(@RequestHeader(value = "X-User-Login") String login) {
        String role = getUserRole(login);
        if (role == null) {
            return ResponseEntity.status(401).body("Utilisateur non trouvé");
        }

        if (!"admin".equals(role)) {
            return ResponseEntity.status(403).body("Accès refusé");
        }

        List<Notification> notifs = notificationRepository.findByIsReadFalseOrderByTimestampDesc();
        return ResponseEntity.ok(notifs);
    }

    @GetMapping("/poll")
    public ResponseEntity<?> pollNewNotifications(
            @RequestHeader(value = "X-User-Login") String login,
            @RequestParam(required = false, defaultValue = "0") Integer lastId) {

        String role = getUserRole(login);
        if (role == null) {
            return ResponseEntity.status(401).body("Utilisateur non trouvé");
        }

        if (!"admin".equals(role)) {
            return ResponseEntity.status(403).body("Accès refusé");
        }

        List<Notification> newNotifs = notificationRepository.findByIsReadFalseAndIdGreaterThanOrderByIdAsc(lastId);
        return ResponseEntity.ok(newNotifs);
    }

    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadCount(@RequestHeader(value = "X-User-Login") String login) {
        String role = getUserRole(login);
        if (role == null) {
            return ResponseEntity.status(401).body("Utilisateur non trouvé");
        }

        if (!"admin".equals(role)) {
            return ResponseEntity.status(403).body("Accès refusé");
        }

        long count = notificationRepository.countByIsReadFalse();

        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Login") String login) {
        String role = getUserRole(login);
        if (role == null) {
            return ResponseEntity.status(401).body("Utilisateur non trouvé");
        }

        if (!"admin".equals(role)) {
            return ResponseEntity.status(403).body("Accès refusé");
        }

        Optional<Notification> opt = notificationRepository.findById(id);
        if (opt.isPresent()) {
            notificationRepository.markAsReadAdmin(id);
            return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Marquée comme lue"));
        }
        return ResponseEntity.status(404).body("Notification introuvable");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Login") String login) {
        String role = getUserRole(login);
        if (role == null) {
            return ResponseEntity.status(401).body("Utilisateur non trouvé");
        }

        if (!"admin".equals(role)) {
            return ResponseEntity.status(403).body("Accès refusé");
        }

        Optional<Notification> opt = notificationRepository.findById(id);
        if (opt.isPresent()) {
            notificationRepository.deleteById(id);
            return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Notification supprimée"));
        }
        return ResponseEntity.status(404).body("Notification introuvable");
    }

    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllNotifications(@RequestHeader(value = "X-User-Login") String login) {
        String role = getUserRole(login);
        if (role == null) {
            return ResponseEntity.status(401).body("Utilisateur non trouvé");
        }

        if (!"admin".equals(role)) {
            return ResponseEntity.status(403).body("Accès refusé");
        }

        notificationRepository.deleteAll();
        return ResponseEntity
                .ok(java.util.Collections.singletonMap("message", "Toutes les notifications ont été supprimées"));
    }
}
