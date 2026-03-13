/**
 * @author Santiago Alexander RODRIGUEZ TRIANA
 * @description JWT servlet filter. Validates the HttpOnly "jwt" cookie on every
 * incoming request. Public routes (POST /auth/login) are whitelisted and bypass
 * the check.
 *
 * On a valid token:
 *   - Extracts claims (userId, login, role, permission)
 *   - Sets them as request attributes so controllers can read them without
 *     re-parsing the token
 *   - Calls chain.doFilter() to continue the request
 *
 * On a missing or invalid token:
 *   - Responds with HTTP 401 and a JSON error body
 *   - Adds a Set-Cookie header to clear any existing (invalid) jwt cookie
 *
 * Registration: see web.xml — mapped to /*
 */
package peps.peps_back.security;

import io.jsonwebtoken.Claims;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtFilter implements Filter {

    // -------------------------------------------------------------------------
    // Public routes — bypass JWT check
    // -------------------------------------------------------------------------
    private static final String LOGIN_PATH = "/auth/login";
    private static final String LOGOUT_PATH = "/auth/logout";

    @Override
    public void init(FilterConfig filterConfig) {
        // No initialization needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getServletPath();

        // Preflight OPTIONS requests must pass through without auth
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // Whitelist: login and logout bypass the filter
        if (isPublic(path, req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // Extract JWT from cookies
        String token = extractJwtCookie(req);
        Claims claims = JwtUtil.validateToken(token);

        if (claims == null) {
            // Token missing, expired, or invalid → reject
            clearJwtCookie(resp);
            sendUnauthorized(resp, "Session expirée ou invalide. Veuillez vous reconnecter.");
            return;
        }

        // Attach claims as request attributes for controllers
        req.setAttribute("userId", claims.get("userId", Long.class));
        req.setAttribute("login", claims.getSubject());
        req.setAttribute("role", claims.get("role", String.class));
        req.setAttribute("permission", claims.get("permission", String.class));

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private boolean isPublic(String path, String method) {
        if (path == null) return false;
        return (path.contains(LOGIN_PATH) && "POST".equalsIgnoreCase(method))
                || (path.contains(LOGOUT_PATH) && "POST".equalsIgnoreCase(method));
    }

    private String extractJwtCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null)
            return null;
        for (Cookie cookie : cookies) {
            if (JwtUtil.COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void clearJwtCookie(HttpServletResponse resp) {
        // Overwrite with an expired cookie so the browser removes it
        resp.addHeader("Set-Cookie",
                JwtUtil.COOKIE_NAME + "=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0");
    }

    private void sendUnauthorized(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(
                "{\"error\":\"" + message.replace("\"", "'") + "\"}");
    }
}
