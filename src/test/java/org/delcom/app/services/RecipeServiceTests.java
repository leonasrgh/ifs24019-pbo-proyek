package org.delcom.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.delcom.app.dto.RecipeForm;
import org.delcom.app.entities.Recipe;
import org.delcom.app.repositories.RecipeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecipeServiceTests {

    @Test
    @DisplayName("Test Recipe Service Logic (CRUD & Stats)")
    void testRecipeService() {
        // 1. Setup Mock Dependencies
        RecipeRepository recipeRepository = mock(RecipeRepository.class);
        FileStorageService fileStorageService = mock(FileStorageService.class);
        
        // Inisialisasi Service dengan Mock
        RecipeService recipeService = new RecipeService(recipeRepository, fileStorageService);

        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        
        // Setup Data Dummy
        Recipe mockRecipe = new Recipe(userId, "Sate Ayam", "Bakar di arang", "Daging Ayam, Kecap");
        mockRecipe.setId(recipeId);

        // ====================================================================
        // TEST CASE 1: CREATE RECIPE (Menggunakan DTO RecipeForm)
        // ====================================================================
        RecipeForm form = new RecipeForm();
        form.setTitle("Sate Ayam");
        form.setDescription("Bakar di arang");
        form.setIngredients("Daging Ayam, Kecap");
        
        when(recipeRepository.save(any(Recipe.class))).thenReturn(mockRecipe);
        
        Recipe created = recipeService.createRecipe(userId, form);
        assertNotNull(created);
        assertEquals("Sate Ayam", created.getTitle());
        assertEquals("Daging Ayam, Kecap", created.getIngredients());

        // ====================================================================
        // TEST CASE 2: GET ALL RECIPES
        // ====================================================================
        // Case: Tanpa Search (Ambil semua punya user)
        when(recipeRepository.findAllByUserId(userId)).thenReturn(List.of(mockRecipe));
        List<Recipe> list = recipeService.getAllRecipes(userId, null);
        assertEquals(1, list.size());

        // Case: Dengan Search (Keyword "Sate")
        when(recipeRepository.findByKeyword(userId, "Sate")).thenReturn(List.of(mockRecipe));
        List<Recipe> searchList = recipeService.getAllRecipes(userId, "Sate");
        assertEquals(1, searchList.size());
        assertEquals("Sate Ayam", searchList.get(0).getTitle());

        // ====================================================================
        // TEST CASE 3: UPDATE RECIPE (Text Data)
        // ====================================================================
        when(recipeRepository.findByUserIdAndId(userId, recipeId)).thenReturn(Optional.of(mockRecipe));
        
        form.setTitle("Sate Kambing"); // Skenario ubah judul
        Recipe updated = recipeService.updateRecipe(userId, recipeId, form);
        
        assertEquals("Sate Kambing", updated.getTitle()); // Verifikasi judul berubah

        // ====================================================================
        // TEST CASE 4: UPDATE COVER IMAGE
        // ====================================================================
        mockRecipe.setCover("old_cover.jpg"); // Pura-pura ada cover lama
        when(fileStorageService.deleteFile("old_cover.jpg")).thenReturn(true); // Mock hapus file lama
        when(recipeRepository.save(any(Recipe.class))).thenReturn(mockRecipe);

        recipeService.updateRecipeImage(userId, recipeId, "new_cover.jpg");
        
        verify(fileStorageService, times(1)).deleteFile("old_cover.jpg"); // Pastikan file lama dihapus
        assertEquals("new_cover.jpg", mockRecipe.getCover());

        // ====================================================================
        // TEST CASE 5: DELETE RECIPE
        // ====================================================================
        when(recipeRepository.findByUserIdAndId(userId, recipeId)).thenReturn(Optional.of(mockRecipe));
        
        boolean deleted = recipeService.deleteRecipe(userId, recipeId);
        assertTrue(deleted);
        verify(recipeRepository, times(1)).deleteById(recipeId); // Pastikan fungsi delete dipanggil

        // ====================================================================
        // TEST CASE 6: GET STATS (Fitur Chart)
        // ====================================================================
        when(recipeRepository.findAllByUserId(userId)).thenReturn(List.of(mockRecipe));
        
        Map<String, Object> stats = recipeService.getStats(userId);
        assertNotNull(stats);
        // Deskripsi "Bakar di arang" < 100 karakter, masuk kategori "Resep Simpel"
        assertEquals(1L, stats.get("shortRecipes")); 
        assertEquals(1L, stats.get("totalRecipes"));
    }
}