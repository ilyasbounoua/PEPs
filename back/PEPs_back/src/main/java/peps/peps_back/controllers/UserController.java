package peps.peps_back.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import peps.peps_back.items.User;
import peps.peps_back.repositories.UserRepository;

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
    public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserRequest request) {
        // Vérifier si le login existe déjà
        if (userRepository.findByLogin(request.getLogin()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = new User();
        user.setLogin(request.getLogin());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(savedUser));
    }

    /**
     * Updates an existing user.
     * Only non-null fields are updated.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Integer id, @RequestBody UpdateUserRequest request) {
        return userRepository.findById(id)
                .map(user -> {
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

                    User updatedUser = userRepository.save(user);
                    return ResponseEntity.ok(toDTO(updatedUser));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a user by their ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
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
                    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(java.util.Collections.singletonMap("error", "Current password is incorrect"));
                    }

                    // Verify new password is valid
                    if (request.getNewPassword() == null || request.getNewPassword().length() < 4) {
                        return ResponseEntity.badRequest()
                                .body(java.util.Collections.singletonMap("error",
                                        "New password must be at least 4 characters"));
                    }

                    // Update password
                    user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
                    userRepository.save(user);

                    return ResponseEntity
                            .ok(java.util.Collections.singletonMap("message", "Password changed successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
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
                user.getEnabled());
    }
}
