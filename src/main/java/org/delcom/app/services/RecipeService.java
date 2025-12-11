package org.delcom.app.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.Optional;
import java.util.UUID;

import org.delcom.app.dto.RecipeForm;
import org.delcom.app.entities.Recipe;
import org.delcom.app.repositories.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final FileStorageService fileStorageService;

    public RecipeService(RecipeRepository recipeRepository, FileStorageService fileStorageService) {
        this.recipeRepository = recipeRepository;
        this.fileStorageService = fileStorageService;
    }

    // CREATE RECIPE (Menggunakan DTO)
    @Transactional
    public Recipe createRecipe(UUID userId, RecipeForm form) {
        Recipe recipe = new Recipe(userId, form.getTitle(), form.getDescription(), form.getIngredients());
        return recipeRepository.save(recipe);
    }

    // READ ALL RECIPES (SEARCH FEATURE)
    public List<Recipe> getAllRecipes(UUID userId, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return recipeRepository.findByKeyword(userId, search);
        }
        return recipeRepository.findAllByUserId(userId);
    }

    // READ SINGLE RECIPE (BY ID)
    public Recipe getRecipeById(UUID userId, UUID id) {
        return recipeRepository.findByUserIdAndId(userId, id).orElse(null);
    }
    
    // READ SINGLE RECIPE (ONLY BY ID, FOR INTERNAL USE)
    public Recipe getRecipeById(UUID id) {
        return recipeRepository.findById(id).orElse(null);
    }

    // UPDATE RECIPE (TEXT DATA)
    @Transactional
    public Recipe updateRecipe(UUID userId, UUID id, RecipeForm form) {
        Recipe recipe = recipeRepository.findByUserIdAndId(userId, id).orElse(null);
        if (recipe != null) {
            recipe.setTitle(form.getTitle());
            recipe.setDescription(form.getDescription());
            recipe.setIngredients(form.getIngredients());
            return recipeRepository.save(recipe);
        }
        return null;
    }

    // DELETE RECIPE
    @Transactional
    public boolean deleteRecipe(UUID userId, UUID id) {
        Recipe recipe = recipeRepository.findByUserIdAndId(userId, id).orElse(null);
        if (recipe == null) {
            return false;
        }

        // Hapus juga file gambar cover jika ada
        if (recipe.getCover() != null) {
            fileStorageService.deleteFile(recipe.getCover());
        }

        recipeRepository.deleteById(id);
        return true;
    }

    // UPDATE RECIPE COVER (IMAGE)
    @Transactional
    public Recipe updateRecipeImage(UUID userId, UUID recipeId, String coverFilename) {
        Recipe recipe = recipeRepository.findByUserIdAndId(userId, recipeId).orElse(null);
        if (recipe != null) {

            // Hapus file cover lama jika ada (Clean up storage)
            if (recipe.getCover() != null) {
                fileStorageService.deleteFile(recipe.getCover());
            }

            recipe.setCover(coverFilename);
            return recipeRepository.save(recipe);
        }
        return null;
    }

    // GET STATISTICS FOR CHART (NEW FEATURE)
    public Map<String, Object> getStats(UUID userId) {
        List<Recipe> recipes = recipeRepository.findAllByUserId(userId);
        
        long totalRecipes = recipes.size();
        
        // Contoh Logika Chart Sederhana: Menghitung resep berdasarkan panjang deskripsi
        // (Misal: Resep Pendek vs Resep Panjang)
        long shortRecipes = recipes.stream().filter(r -> r.getDescription().length() < 100).count();
        long longRecipes = totalRecipes - shortRecipes;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecipes", totalRecipes);
        stats.put("shortRecipes", shortRecipes);
        stats.put("longRecipes", longRecipes);
        
        // Data untuk Bar Chart: Jumlah resep yang dibuat
        stats.put("label", "Statistik Resep");
        
        return stats;
    }
}