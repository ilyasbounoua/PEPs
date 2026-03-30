package peps.peps_back.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour JwtFilter.
 * Vérifie le filtrage des requêtes, la gestion des cookies et la sécurité JWT.
 */
class JwtFilterTest {

    private JwtFilter jwtFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private MockedStatic<JwtUtil> mockedJwtUtil;

    @BeforeEach
    void setUp() {
        jwtFilter = new JwtFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        
        // Mock de la classe statique JwtUtil
        mockedJwtUtil = mockStatic(JwtUtil.class);
    }

    @AfterEach
    void tearDown() {
        // Très important de fermer le mock statique après chaque test
        mockedJwtUtil.close();
    }

    @Test
    @DisplayName("Whitelisting: POST /auth/login should bypass the filter")
    void testDoFilter_LoginPath_Bypass() throws Exception {
        when(request.getServletPath()).thenReturn("/auth/login");
        when(request.getMethod()).thenReturn("POST");

        jwtFilter.doFilter(request, response, chain);

        // Vérifie que le filtre laisse passer la requête au suivant
        verify(chain).doFilter(request, response);
        mockedJwtUtil.verify(() -> JwtUtil.validateToken(anyString()), never());
    }

    @Test
    @DisplayName("CORS: OPTIONS requests should always pass through")
    void testDoFilter_OptionsMethod_Bypass() throws Exception {
        when(request.getMethod()).thenReturn("OPTIONS");

        jwtFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("JWT Valid: Should extract claims and set request attributes")
    void testDoFilter_ValidToken() throws Exception {
        when(request.getServletPath()).thenReturn("/users");
        when(request.getMethod()).thenReturn("GET");

        // Simuler la présence du cookie
        Cookie jwtCookie = new Cookie("jwt", "valid-token");
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});

        // Simuler des Claims valides
        Claims mockClaims = mock(Claims.class);
        when(mockClaims.getSubject()).thenReturn("admin");
        when(mockClaims.get("userId", Long.class)).thenReturn(1L);
        when(mockClaims.get("role", String.class)).thenReturn("admin-role");

        mockedJwtUtil.when(() -> JwtUtil.validateToken("valid-token")).thenReturn(mockClaims);

        jwtFilter.doFilter(request, response, chain);

        
        verify(request).setAttribute("userId", 1L);
        verify(request).setAttribute("login", "admin");
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("JWT Invalid: Should return 401 and clear cookie")
    void testDoFilter_InvalidToken() throws Exception {
        when(request.getServletPath()).thenReturn("/users");
        when(request.getMethod()).thenReturn("GET");
        
        // Simuler un cookie présent mais token invalide
        Cookie jwtCookie = new Cookie("jwt", "bad-token");
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});
        
        mockedJwtUtil.when(() -> JwtUtil.validateToken("bad-token")).thenReturn(null);

        // Capturer la réponse JSON
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        jwtFilter.doFilter(request, response, chain);

        // Vérifie le statut 401
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).addHeader(eq("Set-Cookie"), contains("Max-Age=0"));
        verify(chain, never()).doFilter(any(), any());
        
        assertTrue(stringWriter.toString().contains("Session expirée"));
    }

    @Test
    @DisplayName("IoT Whitelisting: POST /interactions should bypass auth")
    void testDoFilter_IotPath_Bypass() throws Exception {
        when(request.getServletPath()).thenReturn("/interactions");
        when(request.getMethod()).thenReturn("POST");

        jwtFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}