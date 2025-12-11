package org.delcom.app.controllers;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.dto.CoverRecipeForm;
import org.delcom.app.dto.RecipeForm;
import org.delcom.app.entities.Recipe;
import org.delcom.app.services.FileStorageService;
import org.delcom.app.services.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/recipes")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    protected AuthContext authContext;

    public RecipeController(RecipeService recipeService, FileStorageService fileStorageService) {
        this.recipeService = recipeService;
        this.fileStorageService = fileStorageService;
    }

    // ========================================================================
    // 0. HOME / DASHBOARD (Menampilkan Daftar Resep) - NEW!
    // ========================================================================
    @GetMapping("")
    public String home(Model model, @RequestParam(required = false) String search) {
        if (!authContext.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        
        UUID userId = authContext.getAuthUser().getId();
        
        // Ambil data resep & statistik
        List<Recipe> recipes = recipeService.getAllRecipes(userId, search);
        Map<String, Object> stats = recipeService.getStats(userId);
        
        model.addAttribute("recipes", recipes);
        model.addAttribute("stats", stats);
        
        // Objek form untuk Modal Tambah Resep
        model.addAttribute("recipeForm", new RecipeForm()); 
        
        return "pages/recipes/home";
    }

    // ========================================================================
    // 1. ADD RECIPE
    // ========================================================================
    @PostMapping("/add")
    public String createRecipe(@Valid @ModelAttribute("recipeForm") RecipeForm form,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (!authContext.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Data resep tidak valid. Pastikan judul & bahan terisi.");
            redirectAttributes.addFlashAttribute("addRecipeModalOpen", true);
            return "redirect:/recipes"; 
        }

        try {
            recipeService.createRecipe(authContext.getAuthUser().getId(), form);
            redirectAttributes.addFlashAttribute("success", "Resep berhasil dibuat!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal membuat resep: " + e.getMessage());
        }

        return "redirect:/recipes";
    }

    // ========================================================================
    // 2. DETAIL RECIPE
    // ========================================================================
    @GetMapping("/{id}")
    public String getDetailRecipe(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        if (!authContext.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        Recipe recipe = recipeService.getRecipeById(authContext.getAuthUser().getId(), id);

        if (recipe == null) {
            redirectAttributes.addFlashAttribute("error", "Resep tidak ditemukan.");
            return "redirect:/recipes";
        }

        model.addAttribute("recipe", recipe);
        
        // Siapkan form untuk modal-modal edit/delete
        RecipeForm editForm = new RecipeForm();
        editForm.setId(recipe.getId());
        editForm.setTitle(recipe.getTitle());
        editForm.setDescription(recipe.getDescription());
        editForm.setIngredients(recipe.getIngredients());
        model.addAttribute("recipeForm", editForm);

        CoverRecipeForm coverForm = new CoverRecipeForm();
        coverForm.setId(recipe.getId());
        model.addAttribute("coverRecipeForm", coverForm);

        RecipeForm deleteForm = new RecipeForm();
        deleteForm.setId(recipe.getId());
        model.addAttribute("deleteRecipeForm", deleteForm);

        return "pages/recipes/detail";
    }

    // ========================================================================
    // 3. EDIT RECIPE TEXT
    // ========================================================================
    @PostMapping("/edit")
    public String updateRecipe(@Valid @ModelAttribute("recipeForm") RecipeForm form,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (!authContext.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Gagal update: Input tidak valid.");
            return "redirect:/recipes/" + form.getId();
        }

        Recipe updated = recipeService.updateRecipe(authContext.getAuthUser().getId(), form.getId(), form);
        
        if (updated != null) {
            redirectAttributes.addFlashAttribute("success", "Resep berhasil diperbarui!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Gagal: Resep tidak ditemukan.");
        }

        return "redirect:/recipes/" + form.getId();
    }

    // ========================================================================
    // 4. EDIT COVER IMAGE
    // ========================================================================
    @PostMapping("/edit-cover")
    public String editCover(@Valid @ModelAttribute("coverRecipeForm") CoverRecipeForm form,
                            BindingResult result,
                            RedirectAttributes redirectAttributes) {
        if (!authContext.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        if (form.getCoverFile() == null || form.getCoverFile().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Pilih file gambar terlebih dahulu.");
            return "redirect:/recipes/" + form.getId();
        }

        if (!form.isValidImage()) {
            redirectAttributes.addFlashAttribute("error", "Format file harus JPG, PNG, atau GIF.");
            return "redirect:/recipes/" + form.getId();
        }

        try {
            // Parameter storeFile sudah benar (File, UUID)
            String filename = fileStorageService.storeFile(form.getCoverFile(), form.getId());
            
            recipeService.updateRecipeImage(authContext.getAuthUser().getId(), form.getId(), filename);
            redirectAttributes.addFlashAttribute("success", "Foto sampul berhasil diperbarui!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Upload gagal: " + e.getMessage());
        }

        return "redirect:/recipes/" + form.getId();
    }

    // ========================================================================
    // 5. DELETE RECIPE
    // ========================================================================
    @PostMapping("/delete")
    public String deleteRecipe(@ModelAttribute("deleteRecipeForm") RecipeForm form,
                               RedirectAttributes redirectAttributes) {
        if (!authContext.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        boolean deleted = recipeService.deleteRecipe(authContext.getAuthUser().getId(), form.getId());

        if (deleted) {
            redirectAttributes.addFlashAttribute("success", "Resep berhasil dihapus.");
            return "redirect:/recipes";
        } else {
            redirectAttributes.addFlashAttribute("error", "Gagal menghapus resep.");
            return "redirect:/recipes/" + form.getId();
        }
    }

    // ========================================================================
    // 6. SERVE IMAGE (Dengan Handling Error MalformedURL)
    // ========================================================================
    @GetMapping("/cover/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Resource file = new UrlResource(
                    fileStorageService.loadFile(filename).toUri()
            );
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================================================
    // 7. API STATS (JSON)
    // ========================================================================
    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<?> getRecipeStats() {
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("status", "fail"));
        }
        Map<String, Object> stats = recipeService.getStats(authContext.getAuthUser().getId());
        return ResponseEntity.ok(Map.of("status", "success", "data", stats));
    }
    
    // Stub methods untuk kompatibilitas test lama (Generics fixed)
    public ResponseEntity<?> getAllRecipes(Object o) { return null; }
    public ResponseEntity<?> getRecipeById(UUID id) { return null; }
}