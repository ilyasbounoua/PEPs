package peps.peps_back.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import peps.peps_back.items.User;
import peps.peps_back.repositories.UserRepository;
import peps.peps_back.services.AuditService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for user management (CRUD).
 * Accessible only by administrators.
 * 
 * Endpoints:
 * - GET /users : List all users
 * - GET /users/{id} : Get user by ID
 * - POST /users : Create a new user
 * - PUT /users/{id} : Update an existing user
 * - DELETE /users/{id} : Delete a user
 * - PUT /users/{id}/password : Change user password
 * 
 * Note: Role-based filtering should be added via Spring Security
 * once JWT authentication is implemented.
 * 
 * @author Equipe PEP'S, Anas EL HOUDI
 */
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

        private final UserRepository userRepository;
        private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        @Autowired
        private AuditService auditService;

        public UserController(UserRepository userRepository) {
                this.userRepository = userRepository;
        }

        /**
         * Lists all users.
         * Returns a list of UserDTO (without passwords).
         */
        @GetMapping
        public ResponseEntity<List<UserDTO>> getAllUsers() {
                List<UserDTO> users = userRepository.findAll().stream()
                                .sorted((u1, u2) -> {
                                        // Priority: Editor (2) > Viewer (1)
                                        // Note: 'admin' permission is deprecated/removed, treated as 'editor' for
                                        // sorting if present
                                        int s1 = "editor".equalsIgnoreCase(u1.getPermission())
                                                        || "admin".equalsIgnoreCase(u1.getPermission()) ? 2 : 1;
                                        int s2 = "editor".equalsIgnoreCase(u2.getPermission())
                                                        || "admin".equalsIgnoreCase(u2.getPermission()) ? 2 : 1;
                                        return Integer.compare(s2, s1);
                                })
                                .map(this::toDTO)
                                .collect(Collectors.toList());
                return ResponseEntity.ok(users);
        }

        /**
         * Gets a user by their ID.
         */
        @GetMapping("/{id}")
        public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {
                return userRepository.findById(id)
                                .map(user -> ResponseEntity.ok(toDTO(user)))
                                .orElse(ResponseEntity.notFound().build());
        }

        /**
         * Creates a new user.
         * Password is hashed with BCrypt before storage.
         */
        @PostMapping
        public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserRequest request,
                        @RequestHeader(value = "X-User-Login", required = false) String login) {
                // Vérifier si le login existe déjà
                if (userRepository.findByLogin(request.getLogin()).isPresent()) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).build();
                }

                User user = new User();
                user.setLogin(request.getLogin());
                user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
                user.setRole(request.getRole());
                // Default permission to 'viewer' if not provided
                String permission = request.getPermission() != null ? request.getPermission() : "viewer";
                user.setPermission(permission);
                user.setEnabled(true);

                User savedUser = userRepository.save(user);

                // Log creation in audit
                String newValue = String.format(
                                "{\"login\":\"%s\",\"role\":\"%s\",\"permission\":\"%s\"}",
                                savedUser.getLogin(), savedUser.getRole(), savedUser.getPermission());
                String userLogin = (login != null) ? login : "unknown";
                auditService.log("CREATE", "user", savedUser.getIdUser(), savedUser.getLogin(),
                                savedUser.getRole(), userLogin, null, newValue, "Création d'un utilisateur");

                return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(savedUser));
        }

        /**
         * Updates an existing user.
         * Only non-null fields are updated.
         */
        @PutMapping("/{id}")
        public ResponseEntity<UserDTO> updateUser(@PathVariable Integer id, @RequestBody UpdateUserRequest request,
                        @RequestHeader(value = "X-User-Login", required = false) String login) {
                return userRepository.findById(id)
                                .map(user -> {
                                        // Capture old values
                                        String oldValue = String.format(
                                                        "{\"login\":\"%s\",\"role\":\"%s\",\"permission\":\"%s\"}",
                                                        user.getLogin(), user.getRole(), user.getPermission());

                                        // Mise à jour conditionnelle des champs
                                        if (request.getLogin() != null && !request.getLogin().isEmpty()) {
                                                user.setLogin(request.getLogin());
                                        }
                                        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                                                user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
                                        }
                                        if (request.getRole() != null && !request.getRole().isEmpty()) {
                                                user.setRole(request.getRole());
                                        }
                                        if (request.getPermission() != null && !request.getPermission().isEmpty()) {
                                                user.setPermission(request.getPermission());
                                        }

                                        User updatedUser = userRepository.save(user);

                                        // Log update
                                        String newValue = String.format(
                                                        "{\"login\":\"%s\",\"role\":\"%s\",\"permission\":\"%s\"}",
                                                        updatedUser.getLogin(), updatedUser.getRole(),
                                                        updatedUser.getPermission());
                                        String userLogin = (login != null) ? login : "unknown";
                                        auditService.log("UPDATE", "user", updatedUser.getIdUser(),
                                                        updatedUser.getLogin(),
                                                        updatedUser.getRole(), userLogin, oldValue, newValue,
                                                        "Modification de l'utilisateur");

                                        return ResponseEntity.ok(toDTO(updatedUser));
                                })
                                .orElse(ResponseEntity.notFound().build());
        }

        /**
         * Deletes a user by their ID.
         */
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteUser(@PathVariable Integer id,
                        @RequestHeader(value = "X-User-Login", required = false) String login) {
                return userRepository.findById(id)
                                .map(user -> {
                                        // Log deletion in audit
                                        String oldValue = String.format(
                                                        "{\"login\":\"%s\",\"role\":\"%s\"}",
                                                        user.getLogin(), user.getRole());
                                        String userLogin = (login != null) ? login : "unknown";
                                        auditService.log("DELETE", "user", id, user.getLogin(),
                                                        user.getRole(), userLogin, oldValue, null,
                                                        "Suppression de l'utilisateur");

                                        userRepository.delete(user);
                                        return ResponseEntity.ok().<Void>build();
                                })
                                .orElse(ResponseEntity.notFound().build());
        }

        /* ===================== */
        /* User Password Change */
        /* ===================== */

        /**
         * Allows a user to change their own password.
         * Verifies that the current password is correct before replacing it.
         * 
         * @param id      User ID
         * @param request Contains currentPassword and newPassword
         * @author Anas EL HOUDI
         */
        @PutMapping("/{id}/password")
        public ResponseEntity<?> changePassword(@PathVariable Integer id, @RequestBody ChangePasswordRequest request) {
                return userRepository.findById(id)
                                .map(user -> {
                                        // Verify current password is correct
                                        if (!passwordEncoder.matches(request.getCurrentPassword(),
                                                        user.getPasswordHash())) {
                                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                                                .body(java.util.Collections.singletonMap("error",
                                                                                "Current password is incorrect"));
                                        }

                                        // Verify new password is valid
                                        if (request.getNewPassword() == null || request.getNewPassword().length() < 4) {
                                                return ResponseEntity.badRequest()
                                                                .body(java.util.Collections.singletonMap("error",
                                                                                "New password must be at least 4 characters"));
                                        }

                                        // Capture old value (hash only)
                                        String oldValue = "{\"password_hash\":\"***\"}";

                                        // Update password
                                        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
                                        userRepository.save(user);

                                        // Log password change
                                        // Note: Here, the user changing the password is the user themselves (usually)
                                        String userLogin = user.getLogin(); // Self-change
                                        auditService.log("UPDATE", "user", user.getIdUser(), user.getLogin(),
                                                        user.getRole(), userLogin, oldValue, null,
                                                        "Changement de mot de passe");

                                        return ResponseEntity
                                                        .ok(java.util.Collections.singletonMap("message",
                                                                        "Password changed successfully"));
                                })
                                .orElse(ResponseEntity.notFound().build());
        }

        /* ===================== */
        /* User Language Preference */
        /* ===================== */

        /**
         * Updates the user's preferred display language.
         * 
         * @param id   User ID
         * @param body JSON with "lang" key ("fr" or "en")
         * @author Anas EL HOUDI
         */
        @PutMapping("/{id}/language")
        public ResponseEntity<?> changeLanguage(@PathVariable Integer id,
                        @RequestBody java.util.Map<String, String> body) {
                String lang = body.get("lang");
                if (lang == null || (!lang.equals("fr") && !lang.equals("en"))) {
                        return ResponseEntity.badRequest()
                                        .body(java.util.Collections.singletonMap("error",
                                                        "Language must be 'fr' or 'en'"));
                }
                return userRepository.findById(id)
                                .map(user -> {
                                        user.setPreferredLang(lang);
                                        userRepository.save(user);
                                        return ResponseEntity.ok(java.util.Collections.singletonMap("message",
                                                        "Language updated to " + lang));
                                })
                                .orElse(ResponseEntity.notFound().build());
        }

        /* ===================== */
        /* Migration Utility */
        /* ===================== */

        /**
         * Temporary endpoint to migrate users with 'admin' permission to 'editor'.
         * 'admin' role users already have full access, so permission becomes 'editor'.
         */
        @PostMapping("/migrate-permissions")
        public ResponseEntity<?> migratePermissions() {
                List<User> users = userRepository.findAll();
                int updatedCount = 0;

                for (User user : users) {
                        if ("admin".equalsIgnoreCase(user.getPermission())) {
                                user.setPermission("editor");
                                userRepository.save(user);
                                updatedCount++;
                        }
                }

                return ResponseEntity.ok(java.util.Collections.singletonMap("message",
                                "Migration complete. Updated " + updatedCount
                                                + " users from ADMIN to EDITOR permission."));
        }

        /* ===================== */
        /* Utility Method */
        /* ===================== */

        /**
         * Converts a User entity to UserDTO.
         * Hides password_hash for security reasons.
         */
        private UserDTO toDTO(User user) {
                return new UserDTO(
                                user.getIdUser(),
                                user.getLogin(),
                                user.getRole(),
                                user.getPermission(),
                                user.getEnabled());
        }
}
