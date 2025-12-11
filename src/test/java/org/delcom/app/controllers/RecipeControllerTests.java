package org.delcom.app.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.dto.CoverRecipeForm;
import org.delcom.app.dto.RecipeForm;
import org.delcom.app.entities.Recipe;
import org.delcom.app.entities.User;
import org.delcom.app.services.FileStorageService;
import org.delcom.app.services.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

class RecipeControllerTests {

    private RecipeController recipeController;
    private RecipeService recipeService;
    private FileStorageService fileStorageService;
    private AuthContext authContext;

    // Mocks untuk Parameter Controller
    private BindingResult bindingResult;
    private RedirectAttributes redirectAttributes;
    private Model model;

    private User authUser;
    private UUID userId;
    private UUID recipeId;

    @BeforeEach
    void setUp() {
        // 1. Setup Mock Services
        recipeService = mock(RecipeService.class);
        fileStorageService = mock(FileStorageService.class);
        authContext = new AuthContext(); // Gunakan real object untuk state sederhana

        // 2. Setup Controller
        recipeController = new RecipeController(recipeService, fileStorageService);
        
        // Inject AuthContext ke Controller (Manual Injection)
        recipeController.authContext = authContext;

        // 3. Setup Mocks Parameter MVC
        bindingResult = mock(BindingResult.class);
        redirectAttributes = mock(RedirectAttributes.class);
        model = mock(Model.class);

        // 4. Setup Data Dummy User & Login
        userId = UUID.randomUUID();
        recipeId = UUID.randomUUID();
        authUser = new User("Chef Juna", "juna@email.com", "pass");
        authUser.setId(userId);
        
        // Default: Login User
        authContext.setAuthUser(authUser);
    }

    @Test
    @DisplayName("Test Create Recipe - Sukses")
    void testCreateRecipe_Success() {
        RecipeForm form = new RecipeForm();
        form.setTitle("Nasi Goreng");

        // Mocking: Tidak ada error validasi
        when(bindingResult.hasErrors()).thenReturn(false);

        // Act
        String viewName = recipeController.createRecipe(form, bindingResult, redirectAttributes);

        // Assert
        assertEquals("redirect:/recipes", viewName); // Harus redirect ke list
        verify(recipeService).createRecipe(eq(userId), eq(form)); // Pastikan service dipanggil
    }

    @Test
    @DisplayName("Test Create Recipe - Gagal Validasi")
    void testCreateRecipe_ValidationError() {
        RecipeForm form = new RecipeForm();
        // Mocking: Ada error validasi
        when(bindingResult.hasErrors()).thenReturn(true);

        // Act
        String viewName = recipeController.createRecipe(form, bindingResult, redirectAttributes);

        // Assert
        assertEquals("redirect:/recipes", viewName);
        // Pastikan flash attribute error ditambahkan
        verify(redirectAttributes).addFlashAttribute(eq("error"), any());
        verify(redirectAttributes).addFlashAttribute(eq("addRecipeModalOpen"), eq(true));
    }

    @Test
    @DisplayName("Test Detail Recipe - Sukses")
    void testGetDetailRecipe_Success() {
        Recipe recipe = new Recipe();
        recipe.setId(recipeId);
        recipe.setTitle("Test Recipe");

        when(recipeService.getRecipeById(userId, recipeId)).thenReturn(recipe);

        // Act
        String viewName = recipeController.getDetailRecipe(recipeId, model, redirectAttributes);

        // Assert
        assertEquals("pages/recipes/detail", viewName);
        verify(model).addAttribute(eq("recipe"), eq(recipe));
    }

    @Test
    @DisplayName("Test Update Recipe - Sukses")
    void testUpdateRecipe_Success() {
        RecipeForm form = new RecipeForm();
        form.setId(recipeId);
        form.setTitle("Updated Title");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(recipeService.updateRecipe(eq(userId), eq(recipeId), eq(form))).thenReturn(new Recipe());

        // Act
        String viewName = recipeController.updateRecipe(form, bindingResult, redirectAttributes);

        // Assert
        assertEquals("redirect:/recipes/" + recipeId, viewName);
        verify(redirectAttributes).addFlashAttribute(eq("success"), any());
    }

    @Test
    @DisplayName("Test Update Cover Image - Sukses")
    void testEditCover_Success() throws Exception {
        // Setup Form dengan Mock File (Manual karena MockMultipartFile butuh dependency spring-test)
        CoverRecipeForm form = mock(CoverRecipeForm.class);
        org.springframework.web.multipart.MultipartFile file = mock(org.springframework.web.multipart.MultipartFile.class);

        when(form.getId()).thenReturn(recipeId);
        when(form.getCoverFile()).thenReturn(file);
        
        // Mock Validasi Manual di Controller
        when(file.isEmpty()).thenReturn(false);
        when(form.isValidImage()).thenReturn(true);
        when(form.isSizeValid(any(Long.class))).thenReturn(true);

        // Mock Service
        when(fileStorageService.storeFile(file, recipeId)).thenReturn("new_cover.jpg");

        // Act
        String viewName = recipeController.editCover(form, bindingResult, redirectAttributes);

        // Assert
        assertEquals("redirect:/recipes/" + recipeId, viewName);
        verify(recipeService).updateRecipeImage(userId, recipeId, "new_cover.jpg");
    }

    @Test
    @DisplayName("Test Delete Recipe - Sukses")
    void testDeleteRecipe_Success() {
        RecipeForm form = new RecipeForm();
        form.setId(recipeId);

        when(recipeService.deleteRecipe(userId, recipeId)).thenReturn(true);

        // Act
        String viewName = recipeController.deleteRecipe(form, redirectAttributes);

        // Assert
        assertEquals("redirect:/recipes", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("success"), any());
    }

    @Test
    @SuppressWarnings("unchecked") // Untuk menghilangkan warning cast Map
    @DisplayName("Test API Stats - Sukses (Mengembalikan ResponseEntity)")
    void testGetStats_Success() {
        Map<String, Object> mockStats = Map.of("total", 10);
        when(recipeService.getStats(userId)).thenReturn(mockStats);

        // Act
        ResponseEntity<?> response = recipeController.getRecipeStats();

        // Assert
        assertEquals(200, response.getStatusCode().value());
        
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("success", body.get("status"));
        assertEquals(mockStats, body.get("data"));
    }

    @Test
    @DisplayName("Test Akses Tanpa Login (Security Check)")
    void testAccessWithoutLogin() {
        authContext.setAuthUser(null); // Logout

        RecipeForm form = new RecipeForm();
        
        // Cek Create
        String view1 = recipeController.createRecipe(form, bindingResult, redirectAttributes);
        assertEquals("redirect:/auth/login", view1);

        // Cek Detail
        String view2 = recipeController.getDetailRecipe(recipeId, model, redirectAttributes);
        assertEquals("redirect:/auth/login", view2);
    }
}