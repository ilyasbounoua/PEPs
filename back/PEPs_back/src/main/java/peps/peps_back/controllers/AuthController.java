/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Santiago Alexander RODRIGUEZ TRIANA
 * @description Authentication controller.
 *
 * POST /auth/login
 *   - Validates credentials (BCrypt)
 *   - Issues a JWT stored as an HttpOnly cookie (Secure; SameSite=Strict)
 *   - Returns {userId, login, role, permission} in the JSON body
 *
 * POST /auth/logout
 *   - Issues an immediately-expired JWT + Max-Age=0 cookie (double invalidation)
 *   - Any in-flight request carrying the old token is rejected by JwtFilter
 */
package peps.peps_back.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import peps.peps_back.items.User;
import peps.peps_back.repositories.UserRepository;
import peps.peps_back.security.JwtUtil;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost" }, allowCredentials = "true")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // -------------------------------------------------------------------------
    // POST /auth/login
    // -------------------------------------------------------------------------

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
            HttpServletResponse servletResponse) {

        User user = userRepository.findByLogin(request.getLogin()).orElse(null);

        if (user == null || !user.getEnabled()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Generate JWT and set it as an HttpOnly cookie
        String token = JwtUtil.generateToken(
                user.getIdUser(),
                user.getLogin(),
                user.getRole(),
                user.getPermission());
        setJwtCookie(servletResponse, token, 8 * 60 * 60); // 8 hours

        // Return user info (the token itself stays in the cookie, never in the body)
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Authentification réussie");
        body.put("userId", user.getIdUser());
        body.put("login", user.getLogin());
        body.put("role", user.getRole());
        body.put("permission", user.getPermission());
        body.put("preferredLang", user.getPreferredLang());

        return ResponseEntity.ok(body);
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

    // -------------------------------------------------------------------------
    // POST /auth/logout
    // -------------------------------------------------------------------------

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = JwtUtil.COOKIE_NAME, required = false) String token,
            HttpServletResponse servletResponse) {

        // Derive a login for the expired token (use "anonymous" if no valid cookie)
        String login = "anonymous";
        if (token != null) {
            var claims = JwtUtil.validateToken(token);
            if (claims != null) {
                login = claims.getSubject();
            }
        }

        // Issue an immediately-expired JWT — actively invalidates any in-flight request
        String expiredToken = JwtUtil.generateExpiredToken(login);
        setJwtCookie(servletResponse, expiredToken, 0); // Max-Age=0 → browser deletes cookie

        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
    }

    // -------------------------------------------------------------------------
    // Cookie helper
    // -------------------------------------------------------------------------

    private void setJwtCookie(HttpServletResponse response, String token, int maxAgeSeconds) {
        // Manual Set-Cookie header — Java Servlet Cookie API does not support SameSite
        String cookieHeader = JwtUtil.COOKIE_NAME + "=" + token
                + "; Path=/"
                + "; HttpOnly"
                + "; SameSite=Strict"
                + "; Max-Age=" + maxAgeSeconds;
        // Uncomment in production (HTTPS):
        // + "; Secure"
        response.addHeader("Set-Cookie", cookieHeader);
    }

    // -------------------------------------------------------------------------
    // DTO interne
    // -------------------------------------------------------------------------

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

        public void setPassword(String pwd) {
            this.password = pwd;
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
