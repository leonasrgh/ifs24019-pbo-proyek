package org.delcom.app.controllers;

import java.util.Map;
import java.util.UUID;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.dto.LoginForm;    // MENGGUNAKAN DTO
import org.delcom.app.dto.RegisterForm; // MENGGUNAKAN DTO
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder; // GUNAKAN INTERFACE INI
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {
    
    private final UserService userService;
    private final AuthTokenService authTokenService;
    private final PasswordEncoder passwordEncoder; // INJECT BEAN DARI SECURITY CONFIG

    // Constructor Injection
    public UserController(UserService userService, AuthTokenService authTokenService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.authTokenService = authTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    protected AuthContext authContext;

    // ========================================================================
    // REGISTRASI (Gunakan RegisterForm)
    // ========================================================================
    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<Map<String, UUID>>> registerUser(@RequestBody RegisterForm form) {
        
        // Validasi
        if (form.getName() == null || form.getName().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Nama tidak boleh kosong", null));
        } else if (form.getEmail() == null || form.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Email tidak boleh kosong", null));
        } else if (form.getPassword() == null || form.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Password tidak boleh kosong", null));
        }

        // Cek email duplikat
        User existingUser = userService.getUserByEmail(form.getEmail());
        if (existingUser != null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("fail", "Email ini sudah terdaftar", null));
        }

        // Encode password menggunakan Bean yang di-inject
        String hashPassword = passwordEncoder.encode(form.getPassword());

        User createdUser = userService.createUser(
                form.getName(),
                form.getEmail(),
                hashPassword);

        return ResponseEntity.ok().body(new ApiResponse<>(
                "success",
                "Registrasi berhasil",
                Map.of("id", createdUser.getId())));
    }

    // ========================================================================
    // LOGIN (Gunakan LoginForm)
    // ========================================================================
    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> loginUser(@RequestBody LoginForm form) {
        
        if (form.getEmail() == null || form.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Email wajib diisi", null));
        } else if (form.getPassword() == null || form.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Password wajib diisi", null));
        }

        User existingUser = userService.getUserByEmail(form.getEmail());
        if (existingUser == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Email atau password salah", null));
        }

        // Cek password match
        boolean isPasswordMatch = passwordEncoder.matches(form.getPassword(), existingUser.getPassword());
        if (!isPasswordMatch) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Email atau password salah", null));
        }

        // Generate Token
        String jwtToken = JwtUtil.generateToken(existingUser.getId());

        // Hapus token lama user ini (Single Session)
        // Jika ingin Multi-Session (bisa login di hp & laptop bersamaan), hapus bagian logic delete ini
        AuthToken existingAuthToken = authTokenService.findUserToken(existingUser.getId(), jwtToken);
        if (existingAuthToken != null) {
            authTokenService.deleteAuthToken(existingUser.getId());
        }

        AuthToken authToken = new AuthToken(existingUser.getId(), jwtToken);
        var createdAuthToken = authTokenService.createAuthToken(authToken);
        
        if (createdAuthToken == null) {
            return ResponseEntity.status(500).body(new ApiResponse<>("error", "Gagal menyimpan token", null));
        }

        return ResponseEntity.ok().body(new ApiResponse<>(
                "success",
                "Login berhasil",
                Map.of("authToken", jwtToken)));
    }

    // ========================================================================
    // GET USER INFO
    // ========================================================================
    @GetMapping("/users/me")
    public ResponseEntity<ApiResponse<Map<String, User>>> getUserInfo() {

        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(401).body(new ApiResponse<>("fail", "Unauthorized", null));
        }
        User authUser = authContext.getAuthUser();
        authUser.setPassword(null); // Security: Jangan kirim hash password ke client

        return ResponseEntity.ok(new ApiResponse<>("success", "Berhasil mengambil data profile",
                Map.of("user", authUser)));
    }

    // ========================================================================
    // UPDATE USER PROFILE
    // ========================================================================
    @PutMapping("/users/me")
    public ResponseEntity<ApiResponse<User>> updateUser(@RequestBody User reqUser) {

        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(401).body(new ApiResponse<>("fail", "Unauthorized", null));
        }
        User authUser = authContext.getAuthUser();

        if (reqUser.getName() == null || reqUser.getName().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Nama tidak boleh kosong", null));
        } else if (reqUser.getEmail() == null || reqUser.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Email tidak boleh kosong", null));
        }

        User updatedUser = userService.updateUser(
                authUser.getId(),
                reqUser.getName(),
                reqUser.getEmail());
        
        if (updatedUser == null) {
            return ResponseEntity.status(404).body(new ApiResponse<>("fail", "User tidak ditemukan", null));
        }

        return ResponseEntity.ok(new ApiResponse<>("success", "Profile berhasil diupdate", null));
    }

    // ========================================================================
    // UPDATE PASSWORD
    // ========================================================================
    @PutMapping("/users/me/password")
    public ResponseEntity<ApiResponse<Void>> updateUserPassword(@RequestBody Map<String, String> passwordPayload) {

        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(401).body(new ApiResponse<>("fail", "Unauthorized", null));
        }

        User authUser = authContext.getAuthUser();

        String oldPassword = passwordPayload.get("password");
        String newPassword = passwordPayload.get("newPassword");

        if (oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Password lama dan baru wajib diisi", null));
        }

        // Validasi password lama
        if (!passwordEncoder.matches(oldPassword, authUser.getPassword())) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Password lama salah", null));
        }

        // Update ke password baru
        String hashPassword = passwordEncoder.encode(newPassword);
        User updatedUser = userService.updatePassword(authUser.getId(), hashPassword);
        
        if (updatedUser == null) {
            return ResponseEntity.status(404).body(new ApiResponse<>("fail", "Gagal update password", null));
        }

        // Logout user (hapus token) agar login ulang dengan password baru
        authTokenService.deleteAuthToken(authUser.getId());

        return ResponseEntity.ok(new ApiResponse<>("success", "Password berhasil diubah, silakan login ulang", null));
    }
}