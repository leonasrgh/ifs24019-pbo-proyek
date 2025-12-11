package org.delcom.app.interceptors;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.Cookie;
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
        
        // 1. Cek Apakah ini Endpoint Public? (Opsional, jika sudah dihandle WebMvcConfig)
        // Kita biarkan WebMvcConfig yang mengatur exclude path, tapi pengecekan ganda tidak masalah.
        
        // 2. Ekstrak Token (Cari di Header DULU, kalau tidak ada cari di COOKIE)
        String token = extractToken(request);

        // 3. Validasi Keberadaan Token
        if (token == null || token.isEmpty()) {
            handleError(request, response, 401, "Token tidak ditemukan");
            return false;
        }

        // 4. Validasi Format JWT
        if (!JwtUtil.validateToken(token, true)) {
            handleError(request, response, 401, "Token tidak valid atau kadaluarsa");
            return false;
        }

        // 5. Ekstrak UserId
        UUID userId = JwtUtil.extractUserId(token);
        if (userId == null) {
            handleError(request, response, 401, "Format token salah");
            return false;
        }

        // 6. Cek Database (Apakah token valid dan user masih ada?)
        AuthToken authToken = authTokenService.findUserToken(userId, token);
        if (authToken == null) {
            handleError(request, response, 401, "Sesi login tidak ditemukan di database");
            return false;
        }

        User authUser = userService.getUserById(authToken.getUserId());
        if (authUser == null) {
            handleError(request, response, 404, "User tidak ditemukan");
            return false;
        }

        // 7. Simpan User ke Context (Agar bisa dipanggil di Controller)
        authContext.setAuthUser(authUser);
        return true;
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private String extractToken(HttpServletRequest request) {
        // A. Cek Header (Untuk Postman / API calls)
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // B. Cek Cookie (PENTING UNTUK THYMELEAF / BROWSER)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("AUTH_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void handleError(HttpServletRequest request, HttpServletResponse response, int status, String message) throws Exception {
        String uri = request.getRequestURI();
        
        // Jika request berasal dari Browser (ingin buka halaman HTML), lakukan REDIRECT ke Login
        // Cek jika URL tidak diawali /api (berarti halaman web biasa)
        if (!uri.startsWith("/api")) {
            response.sendRedirect("/auth/login?error=" + message);
        } else {
            // Jika request adalah API (Postman/AJAX), kirim JSON Error
            sendJsonError(response, status, message);
        }
    }

    private void sendJsonError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse = String.format("{\"status\":\"fail\",\"message\":\"%s\",\"data\":null}", message);
        response.getWriter().write(jsonResponse);
    }
}