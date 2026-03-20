package peps.peps_back.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils; // Import important

import peps.peps_back.items.User;
import peps.peps_back.repositories.UserRepository;
import peps.peps_back.services.AuditService;

/**
 * Tests unitaires pour UserController.
 * Vérifie la gestion du CRUD utilisateur, le hachage des mots de passe et l'audit.
 */
public class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserController userController;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

@BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        ReflectionTestUtils.setField(userController, "auditService", auditService);
    }

    @Test
    @DisplayName("GET /users : Should return sorted list of users (Editor first)")
    public void testGetAllUsers_Sorting() {
        User viewer = new User();
        viewer.setPermission("viewer");
        
        User editor = new User();
        editor.setPermission("editor");

        when(userRepository.findAll()).thenReturn(Arrays.asList(viewer, editor));

        ResponseEntity<List<UserDTO>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        // Vérifie le tri : editor doit être en premier (index 0)
        assertEquals("editor", response.getBody().get(0).getPermission());
    }

    @Test
    @DisplayName("GET /users/{id} : Should return 404 when user not found")
    public void testGetUserById_NotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        ResponseEntity<UserDTO> response = userController.getUserById(99);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /users : Should create user and log audit")
    public void testCreateUser_Success() {
        CreateUserRequest request = new CreateUserRequest();
        request.setLogin("new_user");
        request.setPassword("password123");
        request.setRole("user");
        request.setPermission("editor");

        when(userRepository.findByLogin("new_user")).thenReturn(Optional.empty());
        
        User savedUser = new User();
        savedUser.setIdUser(1);
        savedUser.setLogin("new_user");
        savedUser.setRole("user");
        savedUser.setPermission("editor");
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        ResponseEntity<UserDTO> response = userController.createUser(request, "admin_login");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("new_user", response.getBody().getLogin());
        
        // Vérifie que l'audit a été appelé
        verify(auditService).log(eq("CREATE"), eq("user"), anyInt(), anyString(), 
                                anyString(), eq("admin_login"), isNull(), anyString(), anyString());
    }

    @Test
    @DisplayName("POST /users : Should return 409 if login already exists")
    public void testCreateUser_Conflict() {
        CreateUserRequest request = new CreateUserRequest();
        request.setLogin("existing_user");

        when(userRepository.findByLogin("existing_user")).thenReturn(Optional.of(new User()));

        ResponseEntity<UserDTO> response = userController.createUser(request, "admin");

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("PUT /users/{id} : Should update fields and log audit")
    public void testUpdateUser_Success() {
        User existingUser = new User();
        existingUser.setIdUser(1);
        existingUser.setLogin("old_login");
        existingUser.setPermission("viewer");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setLogin("new_login");

        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        ResponseEntity<UserDTO> response = userController.updateUser(1, request, "admin");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("new_login", response.getBody().getLogin());
        verify(auditService).log(eq("UPDATE"), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("DELETE /users/{id} : Should delete user and log audit")
    public void testDeleteUser_Success() {
        User user = new User();
        user.setIdUser(1);
        user.setLogin("to_delete");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        ResponseEntity<Void> response = userController.deleteUser(1, "admin");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).delete(user);
        verify(auditService).log(eq("DELETE"), eq("user"), eq(1), any(), any(), eq("admin"), any(), isNull(), any());
    }

    @Test
    @DisplayName("PUT /users/{id}/password : Should fail if current password is wrong")
    public void testChangePassword_WrongCurrent() {
        User user = new User();
        // Le hash d'origine correspond à "correct_password"
        user.setPasswordHash(passwordEncoder.encode("correct_password"));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrong_password");
        request.setNewPassword("new_password123");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = userController.changePassword(1, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("PUT /users/{id}/password : Should succeed if current password is correct")
    public void testChangePassword_Success() {
        User user = new User();
        String currentPass = "old_pass";
        user.setPasswordHash(passwordEncoder.encode(currentPass));
        user.setLogin("user1");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(currentPass);
        request.setNewPassword("new_pass_secure");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = userController.changePassword(1, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).save(user);
        // Vérifie que l'audit mentionne le changement
        verify(auditService).log(eq("UPDATE"), eq("user"), any(), any(), any(), any(), any(), isNull(), contains("mot de passe"));
    }

    @Test
    @DisplayName("POST /migrate-permissions : Should migrate ADMIN to EDITOR")
    public void testMigratePermissions() {
        User adminUser = new User();
        adminUser.setPermission("admin");
        
        User normalUser = new User();
        normalUser.setPermission("viewer");

        when(userRepository.findAll()).thenReturn(Arrays.asList(adminUser, normalUser));

        ResponseEntity<?> response = userController.migratePermissions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("editor", adminUser.getPermission()); // A été modifié
        assertEquals("viewer", normalUser.getPermission()); // Inchangé
        verify(userRepository, times(1)).save(adminUser);
    }
}