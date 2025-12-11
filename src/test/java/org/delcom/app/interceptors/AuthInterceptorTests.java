package org.delcom.app.interceptors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthInterceptorTests {

    private AuthInterceptor authInterceptor;
    private AuthTokenService authTokenService;
    private UserService userService;
    private AuthContext authContext;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setup() throws Exception {
        // Setup Mocks
        authTokenService = mock(AuthTokenService.class);
        userService = mock(UserService.class);
        authContext = new AuthContext();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        // Setup Response Writer (agar tidak error saat sendErrorResponse)
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        // Setup Instance
        authInterceptor = new AuthInterceptor();
        // Inject Mocks manually (or use ReflectionTestUtils)
        ReflectionTestUtils.setField(authInterceptor, "authTokenService", authTokenService);
        ReflectionTestUtils.setField(authInterceptor, "userService", userService);
        ReflectionTestUtils.setField(authInterceptor, "authContext", authContext);
    }

    @Test
    @DisplayName("Gagal jika token tidak ada (Header & Cookie null)")
    void testPreHandle_NoToken_ReturnsFalse() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        boolean result = authInterceptor.preHandle(request, response, null);
        assertFalse(result);
    }

    @Test
    @DisplayName("Berhasil jika token valid ada di Header (Bearer)")
    void testPreHandle_ValidHeaderToken_ReturnsTrue() throws Exception {
        UUID userId = UUID.randomUUID();
        String validToken = JwtUtil.generateToken(userId);
        
        AuthToken authToken = new AuthToken(userId, validToken);
        User user = new User("Test User", "test@mail.com", "pass");

        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        
        when(authTokenService.findUserToken(any(UUID.class), anyString())).thenReturn(authToken);
        when(userService.getUserById(userId)).thenReturn(user);

        boolean result = authInterceptor.preHandle(request, response, null);
        assertTrue(result);
    }

    @Test
    @DisplayName("Berhasil jika token valid ada di Cookie")
    void testPreHandle_ValidCookieToken_ReturnsTrue() throws Exception {
        UUID userId = UUID.randomUUID();
        String validToken = JwtUtil.generateToken(userId);
        
        AuthToken authToken = new AuthToken(userId, validToken);
        User user = new User("Test User", "test@mail.com", "pass");

        // Set Cookie
        Cookie authCookie = new Cookie("AUTH_TOKEN", validToken);
        
        when(request.getRequestURI()).thenReturn("/pages/home");
        when(request.getHeader("Authorization")).thenReturn(null); // Header kosong
        when(request.getCookies()).thenReturn(new Cookie[]{authCookie}); // Cookie ada
        
        when(authTokenService.findUserToken(any(UUID.class), anyString())).thenReturn(authToken);
        when(userService.getUserById(userId)).thenReturn(user);

        boolean result = authInterceptor.preHandle(request, response, null);
        assertTrue(result);
    }

    @Test
    @DisplayName("Gagal jika token expired/invalid")
    void testPreHandle_InvalidToken_ReturnsFalse() throws Exception {
        String invalidToken = "invalid.jwt.token";

        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);

        boolean result = authInterceptor.preHandle(request, response, null);
        assertFalse(result);
    }

    @Test
    @DisplayName("Gagal jika token valid tapi tidak ada di database (Logout/Sesi Habis)")
    void testPreHandle_TokenNotInDB_ReturnsFalse() throws Exception {
        UUID userId = UUID.randomUUID();
        String validToken = JwtUtil.generateToken(userId);

        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        
        // Database return null (Token tidak ditemukan)
        when(authTokenService.findUserToken(any(UUID.class), anyString())).thenReturn(null);

        boolean result = authInterceptor.preHandle(request, response, null);
        assertFalse(result);
    }
}