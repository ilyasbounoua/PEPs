package peps.peps_back.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.test.util.ReflectionTestUtils;

import peps.peps_back.items.User;
import peps.peps_back.repositories.UserRepository;

/**
 * Tests unitaires pour AdminInitializer.
 * Vérifie la création, la mise à jour et la sécurité du compte admin au démarrage.
 */
public class AdminInitializerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminInitializer adminInitializer;

    private final String TEST_ADMIN = "admin_test";
    private final String TEST_PASS = "secure_password_123";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Injection manuelle des valeurs @Value via ReflectionTestUtils
        ReflectionTestUtils.setField(adminInitializer, "adminUsername", TEST_ADMIN);
        ReflectionTestUtils.setField(adminInitializer, "adminPassword", TEST_PASS);
    }

    @Test
    @DisplayName("Initialisation: Should create admin if password is set and user missing")
    public void testOnApplicationEvent_CreateNew() {
        // Simulation : L'utilisateur n'existe pas
        when(userRepository.findByLogin(TEST_ADMIN)).thenReturn(Optional.empty());

        adminInitializer.onApplicationEvent(mock(ContextRefreshedEvent.class));

        // Vérification : On a créé l'utilisateur avec les bons rôles
        verify(userRepository).save(argThat(user -> 
            user.getLogin().equals(TEST_ADMIN) && 
            user.getRole().equals("admin") &&
            user.getPermission().equals("admin")
        ));
    }

    @Test
    @DisplayName("Update: Should update password if admin exists but password differs")
    public void testOnApplicationEvent_UpdateExistingPassword() {
        User existingAdmin = new User();
        existingAdmin.setLogin(TEST_ADMIN);
        // On met un vieux hash qui ne correspondra pas à TEST_PASS
        existingAdmin.setPasswordHash("old_different_hash");

        when(userRepository.findByLogin(TEST_ADMIN)).thenReturn(Optional.of(existingAdmin));

        adminInitializer.onApplicationEvent(mock(ContextRefreshedEvent.class));

        // Vérification : save() est appelé pour mettre à jour le mot de passe
        verify(userRepository).save(existingAdmin);
    }

    @Test
    @DisplayName("Skip: Should do nothing if password environment variable is empty")
    public void testOnApplicationEvent_NoPasswordSet() {
        // On vide la variable de mot de passe
        ReflectionTestUtils.setField(adminInitializer, "adminPassword", "");

        adminInitializer.onApplicationEvent(mock(ContextRefreshedEvent.class));

        // Vérification : Le repo n'est jamais sollicité
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Stability: Should not update if admin exists and password already matches")
    public void testOnApplicationEvent_AlreadyUpToDate() {
        User existingAdmin = new User();
        existingAdmin.setLogin(TEST_ADMIN);
        
        // On génère un hash valide pour le mot de passe de test
        // Note: AdminInitializer utilise un BCrypt interne, on simule donc la correspondance
        // car passwordEncoder.matches est utilisé dans la classe.
        // Puisque le BCrypt est "final" dans ta classe, il est plus simple de mocker 
        // le comportement attendu ou de laisser BCrypt faire son travail.
        
        // Ici, on va utiliser un vrai encodeur pour que passwordEncoder.matches retourne true
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder realEncoder = 
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        existingAdmin.setPasswordHash(realEncoder.encode(TEST_PASS));

        when(userRepository.findByLogin(TEST_ADMIN)).thenReturn(Optional.of(existingAdmin));

        adminInitializer.onApplicationEvent(mock(ContextRefreshedEvent.class));

        // Vérification : Pas de save() car tout est déjà conforme
        verify(userRepository, never()).save(any());
    }
}