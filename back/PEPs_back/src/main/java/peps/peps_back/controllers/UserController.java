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
 * Contrôleur REST pour la gestion des utilisateurs (CRUD).
 * Accessible uniquement par les administrateurs.
 * 
 * Endpoints :
 * - GET /users : Liste tous les utilisateurs
 * - GET /users/{id} : Récupère un utilisateur par son ID
 * - POST /users : Crée un nouvel utilisateur
 * - PUT /users/{id} : Modifie un utilisateur existant
 * - DELETE /users/{id} : Supprime un utilisateur
 * 
 * Note : Le filtrage par rôle admin devra être ajouté via Spring Security
 * une fois l'authentification JWT mise en place.
 * 
 * @author Équipe PEP'S, Anas EL HOUDI
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
     * Liste tous les utilisateurs.
     * Retourne une liste de UserDTO (sans les mots de passe).
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    /**
     * Récupère un utilisateur par son ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(toDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crée un nouvel utilisateur.
     * Le mot de passe est hashé avec BCrypt avant stockage.
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
     * Modifie un utilisateur existant.
     * Seuls les champs non-null sont mis à jour.
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
     * Supprime un utilisateur par son ID.
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
    /* Changement de mot de passe (utilisateur) */
    /* ===================== */

    /**
     * Permet à un utilisateur de changer son propre mot de passe.
     * Vérifie que l'ancien mot de passe est correct avant de le remplacer.
     * 
     * @param id      ID de l'utilisateur
     * @param request Contient currentPassword et newPassword
     * @author Anas EL HOUDI
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(@PathVariable Integer id, @RequestBody ChangePasswordRequest request) {
        return userRepository.findById(id)
                .map(user -> {
                    // Vérifier que l'ancien mot de passe est correct
                    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(java.util.Collections.singletonMap("error", "Mot de passe actuel incorrect"));
                    }

                    // Vérifier que le nouveau mot de passe est valide
                    if (request.getNewPassword() == null || request.getNewPassword().length() < 4) {
                        return ResponseEntity.badRequest()
                                .body(java.util.Collections.singletonMap("error",
                                        "Le nouveau mot de passe doit contenir au moins 4 caractères"));
                    }

                    // Mettre à jour le mot de passe
                    user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
                    userRepository.save(user);

                    return ResponseEntity
                            .ok(java.util.Collections.singletonMap("message", "Mot de passe modifié avec succès"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /* ===================== */
    /* Méthode utilitaire */
    /* ===================== */

    /**
     * Convertit une entité User en UserDTO.
     * Masque le password_hash pour des raisons de sécurité.
     */
    private UserDTO toDTO(User user) {
        return new UserDTO(
                user.getIdUser(),
                user.getLogin(),
                user.getRole(),
                user.getEnabled());
    }
}
