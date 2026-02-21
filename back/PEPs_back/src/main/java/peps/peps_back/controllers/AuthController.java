package peps.peps_back.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import peps.peps_back.items.User;
import peps.peps_back.repositories.UserRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        User user = userRepository.findByLogin(request.getLogin()).orElse(null);

        if (user == null || !user.getEnabled()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Réponse d'authentification pour le système multi-profils
        // - userId : identifiant unique pour filtrer les données propres à
        // l'utilisateur
        // - role : "admin", "dauphin" ou "aras" pour contrôler l'accès aux
        // fonctionnalités
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Authentification réussie");
        response.put("userId", user.getIdUser());
        response.put("login", user.getLogin());
        response.put("role", user.getRole());
        response.put("permission", user.getPermission());
        response.put("preferredLang", user.getPreferredLang());

        return ResponseEntity.ok(response);
    }

    /* ===================== */
    /* Password Reset */
    /* ===================== */

    /**
     * Self-service password reset.
     * User provides their login and a new password.
     * No authentication required.
     * 
     * @author Anas EL HOUDI
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (request.getLogin() == null || request.getLogin().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(java.util.Collections.singletonMap("error", "Login is required"));
        }
        if (request.getNewPassword() == null || request.getNewPassword().length() < 4) {
            return ResponseEntity.badRequest()
                    .body(java.util.Collections.singletonMap("error",
                            "New password must be at least 4 characters"));
        }

        User user = userRepository.findByLogin(request.getLogin()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Collections.singletonMap("error", "Login not found"));
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Password reset successfully"));
    }

    /* ===================== */
    /* DTO interne */
    /* ===================== */
    public static class LoginRequest {
        private String login;
        private String password;

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class ResetPasswordRequest {
        private String login;
        private String newPassword;

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}
