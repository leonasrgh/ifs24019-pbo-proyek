package org.delcom.app.interceptors;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication; 
import org.springframework.security.core.context.SecurityContextHolder; 
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    protected AuthContext authContext;

    @Autowired
    protected AuthTokenService authTokenService;

    @Autowired
    protected UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI();

        // 1. SKIP AUTH UNTUK ENDPOINT PUBLIC DAN ASSET
        if (isPublicEndpoint(request) || path.startsWith("/assets")) {
            return true;
        }

        // 2. LOGIKA AUTHENTIKASI BERBASIS SESSION (UNTUK VIEW/DASHBOARD)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Pengecekan Eksplisit: Jika ada Principal yang valid dari Session
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            Object principal = auth.getPrincipal();
            
            if (principal instanceof String email) {
                User authUser = userService.getUserByEmail(email); 
                
                if (authUser != null) {
                    // Berhasil: Set user dan Lanjut
                    authContext.setAuthUser(authUser);
                    return true; 
                }
            }
        }
        
        // 3. LOGIKA AUTHENTIKASI BERBASIS JWT/TOKEN (UNTUK API)
        if (path.startsWith("/api")) { 
            String rawAuthToken = request.getHeader("Authorization");
            String token = extractToken(rawAuthToken);
    
            if (token != null && !token.isEmpty()) {
                
                if (!JwtUtil.validateToken(token, true)) {
                    sendErrorResponse(response, 401, "Token autentikasi tidak valid");
                    return false;
                }
        
                UUID userId = JwtUtil.extractUserId(token);
                if (userId == null) {
                    sendErrorResponse(response, 401, "Format token autentikasi tidak valid");
                    return false;
                }
        
                AuthToken authToken = authTokenService.findUserToken(userId, token);
                if (authToken == null) {
                    sendErrorResponse(response, 401, "Token autentikasi sudah expired");
                    return false;
                }
        
                User authUser = userService.getUserById(authToken.getUserId());
                if (authUser == null) {
                    sendErrorResponse(response, 404, "User tidak ditemukan");
                    return false;
                }
    
                // Berhasil: Set user dan Lanjut
                authContext.setAuthUser(authUser);
                return true; 
            } else {
                 sendErrorResponse(response, 401, "Akses ditolak: Diperlukan autentikasi");
                 return false;
            }
        }

        // 4. JIKA TIDAK TEROTENTIKASI (baik via Session maupun JWT)
        // Biarkan Spring Security yang menangani redirect ke /auth/login untuk View request.
        return true; 
    }

    private String extractToken(String rawAuthToken) {
        if (rawAuthToken != null && rawAuthToken.startsWith("Bearer ")) {
            return rawAuthToken.substring(7);
        }
        return null;
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/api/auth") || path.equals("/error") || path.startsWith("/auth"); 
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"status\":\"fail\",\"message\":\"%s\",\"data\":null}",
                message);
        response.getWriter().write(jsonResponse);
    }
}