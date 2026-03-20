package peps.peps_back.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import peps.peps_back.items.User;
import peps.peps_back.repositories.UserRepository;

/**
 * Unit tests for AuthController.
 */
public class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletResponse response;

    private AuthController authController;
    private BCryptPasswordEncoder realEncoder; // Utilisé pour générer des hash valides dans les tests

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        authController = new AuthController(userRepository);
        realEncoder = new BCryptPasswordEncoder();
    }

    @Test
    @DisplayName("Should return 401 when user is not found")
    public void testLogin_UserNotFound() {
        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setLogin("unknown_user");
        
        when(userRepository.findByLogin("unknown_user")).thenReturn(Optional.empty());

        ResponseEntity<?> result = authController.login(request, response);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertTrue(((Map<?, ?>) result.getBody()).containsKey("error"));
        assertEquals("Utilisateur non trouvé", ((Map<?, ?>) result.getBody()).get("error"));
    }

    @Test
    @DisplayName("Should return 401 when user account is disabled")
    public void testLogin_UserDisabled() {
        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setLogin("disabled_user");

        User disabledUser = new User();
        disabledUser.setLogin("disabled_user");
        disabledUser.setEnabled(false);

        when(userRepository.findByLogin("disabled_user")).thenReturn(Optional.of(disabledUser));

        ResponseEntity<?> result = authController.login(request, response);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertEquals("Compte désactivé", ((Map<?, ?>) result.getBody()).get("error"));
    }

    @Test
    @DisplayName("Should return 401 when password does not match")
    public void testLogin_WrongPassword() {
        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setLogin("user_test");
        request.setPassword("wrong_password");

        User user = new User();
        user.setLogin("user_test");
        user.setEnabled(true);
        user.setPasswordHash(realEncoder.encode("correct_password"));

        when(userRepository.findByLogin("user_test")).thenReturn(Optional.of(user));

        ResponseEntity<?> result = authController.login(request, response);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertEquals("Mot de passe incorrect", ((Map<?, ?>) result.getBody()).get("error"));
    }

    @Test
    @DisplayName("Should login successfully and set cookie when credentials are valid")
    public void testLogin_Success() {
        // Préparation de la requête
        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setLogin("valid_user");
        request.setPassword("password123");

        // Préparation de l'utilisateur simulé
        User user = new User();
        user.setIdUser(1);
        user.setLogin("valid_user");
        user.setRole("ADMIN");
        user.setEnabled(true);
        user.setPasswordHash(realEncoder.encode("password123"));

        when(userRepository.findByLogin("valid_user")).thenReturn(Optional.of(user));

        // Exécution
        ResponseEntity<?> result = authController.login(request, response);

        // Vérifications
        assertEquals(HttpStatus.OK, result.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) result.getBody();
        assertEquals(1, body.get("userId"));
        assertEquals("ADMIN", body.get("role"));
        
        // Vérifie qu'un header Set-Cookie a été ajouté à la réponse
        verify(response).addHeader(eq("Set-Cookie"), contains("HttpOnly"));
        verify(response).addHeader(eq("Set-Cookie"), contains("SameSite=Strict"));
    }

    @Test
    @DisplayName("Should logout and expire the cookie")
    public void testLogout() {
        // L'appel au logout ne nécessite pas de repository
        ResponseEntity<?> result = authController.logout(null, response);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        // Vérifie que le cookie est mis à Max-Age=0 pour suppression
        verify(response).addHeader(eq("Set-Cookie"), contains("Max-Age=0"));
    }

    @Test
    @DisplayName("Should return 401 on /me if no token is provided")
    public void testGetCurrentUser_NoToken() {
        ResponseEntity<?> result = authController.getCurrentUser(null);
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }
}