package org.delcom.app.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.dto.LoginForm;
import org.delcom.app.dto.RegisterForm;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
//import org.delcom.app.utils.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserControllerTests {

    @Test
    @DisplayName("Pengujian UserController dengan DTO dan PasswordEncoder")
    public void testVariousUserController() {

        // 1. Mock Dependencies
        AuthTokenService authTokenService = Mockito.mock(AuthTokenService.class);
        UserService userService = Mockito.mock(UserService.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class); // Dependency Baru

        // 2. Setup Controller
        UserController userController = new UserController(userService, authTokenService, passwordEncoder);
        
        // Inject AuthContext Manual
        userController.authContext = new AuthContext();

        // ====================================================================
        // TEST CASE 1: REGISTER USER (Pakai RegisterForm)
        // ====================================================================
        {
            // A. Gagal: Input Invalid (Nama Kosong)
            RegisterForm invalidForm = new RegisterForm();
            invalidForm.setName(""); 
            invalidForm.setEmail("test@email.com");
            invalidForm.setPassword("123456");

            ResponseEntity<ApiResponse<Map<String, UUID>>> result = userController.registerUser(invalidForm);
            assertEquals(400, result.getStatusCode().value());
            assertTrue(result.getBody().getMessage().contains("Nama"));

            // B. Gagal: Email Sudah Terdaftar
            RegisterForm duplicateForm = new RegisterForm();
            duplicateForm.setName("User Lama");
            duplicateForm.setEmail("lama@email.com");
            duplicateForm.setPassword("123456");

            when(userService.getUserByEmail("lama@email.com")).thenReturn(new User()); // User ditemukan
            
            result = userController.registerUser(duplicateForm);
            assertEquals(400, result.getStatusCode().value());
            assertTrue(result.getBody().getMessage().contains("sudah terdaftar"));

            // C. Sukses: Data Valid
            RegisterForm validForm = new RegisterForm();
            validForm.setName("User Baru");
            validForm.setEmail("baru@email.com");
            validForm.setPassword("123456");

            User createdUser = new User("User Baru", "baru@email.com", "hashed_pwd");
            createdUser.setId(UUID.randomUUID());

            when(userService.getUserByEmail("baru@email.com")).thenReturn(null); // Email belum ada
            when(passwordEncoder.encode("123456")).thenReturn("hashed_pwd");
            when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(createdUser);

            result = userController.registerUser(validForm);
            assertEquals(200, result.getStatusCode().value());
            assertEquals("success", result.getBody().getStatus());
        }

        // ====================================================================
        // TEST CASE 2: LOGIN USER (Pakai LoginForm)
        // ====================================================================
        {
            // A. Gagal: Email Tidak Ditemukan
            LoginForm loginForm = new LoginForm();
            loginForm.setEmail("hantu@email.com");
            loginForm.setPassword("123456");

            when(userService.getUserByEmail("hantu@email.com")).thenReturn(null);

            ResponseEntity<ApiResponse<Map<String, String>>> result = userController.loginUser(loginForm);
            assertEquals(400, result.getStatusCode().value());

            // B. Gagal: Password Salah
            User existingUser = new User("User Asli", "asli@email.com", "hashed_pwd");
            existingUser.setId(UUID.randomUUID());
            
            loginForm.setEmail("asli@email.com");
            
            when(userService.getUserByEmail("asli@email.com")).thenReturn(existingUser);
            when(passwordEncoder.matches("123456", "hashed_pwd")).thenReturn(false); // Password salah

            result = userController.loginUser(loginForm);
            assertEquals(400, result.getStatusCode().value());

            // C. Sukses: Login Berhasil
            when(passwordEncoder.matches("123456", "hashed_pwd")).thenReturn(true); // Password benar
            when(authTokenService.createAuthToken(any(AuthToken.class))).thenReturn(new AuthToken());

            result = userController.loginUser(loginForm);
            assertEquals(200, result.getStatusCode().value());
            assertTrue(result.getBody().getData().containsKey("authToken"));
        }

        // Setup User Terautentikasi untuk tes selanjutnya
        User authUser = new User("Auth User", "auth@email.com", "hashed_pass");
        authUser.setId(UUID.randomUUID());
        userController.authContext.setAuthUser(authUser);

        // ====================================================================
        // TEST CASE 3: GET USER INFO
        // ====================================================================
        {
            var result = userController.getUserInfo();
            assertEquals(200, result.getStatusCode().value());
            assertEquals("Auth User", result.getBody().getData().get("user").getName());
        }

        // ====================================================================
        // TEST CASE 4: UPDATE USER
        // ====================================================================
        {
            User updateReq = new User();
            updateReq.setName("Nama Baru");
            updateReq.setEmail("baru@email.com");

            when(userService.updateUser(eq(authUser.getId()), anyString(), anyString())).thenReturn(authUser);

            var result = userController.updateUser(updateReq);
            assertEquals(200, result.getStatusCode().value());
        }

        // ====================================================================
        // TEST CASE 5: UPDATE PASSWORD
        // ====================================================================
        {
            Map<String, String> payload = Map.of(
                "password", "old_pass",
                "newPassword", "new_pass"
            );

            // A. Gagal: Password Lama Salah
            when(passwordEncoder.matches("old_pass", authUser.getPassword())).thenReturn(false);
            
            var result = userController.updateUserPassword(payload);
            assertEquals(400, result.getStatusCode().value());

            // B. Sukses
            when(passwordEncoder.matches("old_pass", authUser.getPassword())).thenReturn(true);
            when(passwordEncoder.encode("new_pass")).thenReturn("new_hashed_pass");
            when(userService.updatePassword(any(UUID.class), anyString())).thenReturn(authUser);

            result = userController.updateUserPassword(payload);
            assertEquals(200, result.getStatusCode().value());
        }
    }
}