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

        return ResponseEntity.ok(response);
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
}
