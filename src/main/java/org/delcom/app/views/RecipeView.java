package org.delcom.app.views;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import org.delcom.app.dto.CoverRecipeForm;
import org.delcom.app.dto.RecipeForm;
import org.delcom.app.entities.Recipe;
import org.delcom.app.entities.User;
import org.delcom.app.services.FileStorageService;
import org.delcom.app.services.RecipeService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/recipes-backup")
public class RecipeView {

    private final RecipeService recipeService;
    private final FileStorageService fileStorageService;

    public RecipeView(RecipeService recipeService, FileStorageService fileStorageService) {
        this.recipeService = recipeService;
        this.fileStorageService = fileStorageService;
    }

    // Helper untuk ambil User Login
    private User getAuthUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken || authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        return null;
    }

    // ========================================================================
    // ADD RECIPE
    // ========================================================================
    @PostMapping("/add")
    public String postAddRecipe(@Valid @ModelAttribute("recipeForm") RecipeForm recipeForm,
            RedirectAttributes redirectAttributes) {

        User authUser = getAuthUser();
        if (authUser == null) return "redirect:/auth/login";

        // Validasi Manual (Selain @Valid)
        if (recipeForm.getTitle() == null || recipeForm.getTitle().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Judul resep wajib diisi");
            redirectAttributes.addFlashAttribute("addRecipeModalOpen", true);
            return "redirect:/";
        }
        
        // Simpan via Service (Pass DTO langsung sesuai Service yg kita buat sebelumnya)
        var entity = recipeService.createRecipe(authUser.getId(), recipeForm);

        if (entity == null) {
            redirectAttributes.addFlashAttribute("error", "Gagal menambahkan resep");
            return "redirect:/";
        }

        redirectAttributes.addFlashAttribute("success", "Resep berhasil ditambahkan.");
        return "redirect:/";
    }

    // ========================================================================
    // EDIT RECIPE (TEXT)
    // ========================================================================
    @PostMapping("/edit")
    public String postEditRecipe(@Valid @ModelAttribute("recipeForm") RecipeForm recipeForm,
            RedirectAttributes redirectAttributes) {
        
        User authUser = getAuthUser();
        if (authUser == null) return "redirect:/auth/login";

        if (recipeForm.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "ID Resep tidak valid");
            return "redirect:/";
        }

        // Update via Service
        var updated = recipeService.updateRecipe(authUser.getId(), recipeForm.getId(), recipeForm);
        
        if (updated == null) {
            redirectAttributes.addFlashAttribute("error", "Gagal memperbarui resep (Mungkin bukan milik Anda)");
            return "redirect:/";
        }

        redirectAttributes.addFlashAttribute("success", "Resep berhasil diperbarui.");
        // Redirect kembali ke halaman detail
        return "redirect:/recipes/" + recipeForm.getId();
    }

    // ========================================================================
    // DELETE RECIPE
    // ========================================================================
    @PostMapping("/delete")
    public String postDeleteRecipe(@Valid @ModelAttribute("recipeForm") RecipeForm recipeForm,
            RedirectAttributes redirectAttributes) {

        User authUser = getAuthUser();
        if (authUser == null) return "redirect:/auth/login";

        // Cek Resep
        Recipe existingRecipe = recipeService.getRecipeById(authUser.getId(), recipeForm.getId());
        if (existingRecipe == null) {
            redirectAttributes.addFlashAttribute("error", "Resep tidak ditemukan");
            return "redirect:/";
        }

        // Validasi Konfirmasi Judul (Fitur Safety Delete)
        if (!existingRecipe.getTitle().equals(recipeForm.getConfirmTitle())) {
            redirectAttributes.addFlashAttribute("error", "Judul konfirmasi tidak cocok");
            return "redirect:/recipes/" + recipeForm.getId(); // Balik ke detail jika gagal
        }

        boolean deleted = recipeService.deleteRecipe(authUser.getId(), recipeForm.getId());
        if (!deleted) {
            redirectAttributes.addFlashAttribute("error", "Gagal menghapus resep");
            return "redirect:/";
        }

        redirectAttributes.addFlashAttribute("success", "Resep berhasil dihapus.");
        return "redirect:/";
    }

    // ========================================================================
    // DETAIL PAGE
    // ========================================================================
    @GetMapping("/{id}")
    public String getDetailRecipe(@PathVariable UUID id, Model model) {
        User authUser = getAuthUser();
        if (authUser == null) return "redirect:/auth/login";
        
        model.addAttribute("auth", authUser);

        Recipe recipe = recipeService.getRecipeById(authUser.getId(), id);
        if (recipe == null) {
            return "redirect:/"; // Jika tidak ketemu, balik ke home
        }
        model.addAttribute("recipe", recipe);

        // Siapkan Form untuk Edit (Pre-fill data)
        RecipeForm editForm = new RecipeForm();
        editForm.setId(recipe.getId());
        editForm.setTitle(recipe.getTitle());
        editForm.setDescription(recipe.getDescription());
        editForm.setIngredients(recipe.getIngredients());
        model.addAttribute("recipeForm", editForm); // Reuse recipeForm untuk edit text

        // Siapkan Form untuk Delete
        RecipeForm deleteForm = new RecipeForm();
        deleteForm.setId(recipe.getId());
        model.addAttribute("deleteRecipeForm", deleteForm);

        // Siapkan Form untuk Edit Cover
        CoverRecipeForm coverForm = new CoverRecipeForm();
        coverForm.setId(id);
        model.addAttribute("coverRecipeForm", coverForm);

        return ConstUtil.TEMPLATE_PAGES_RECIPES_DETAIL;
    }

    // ========================================================================
    // EDIT COVER (IMAGE)
    // ========================================================================
    @PostMapping("/edit-cover")
    public String postEditCoverRecipe(@Valid @ModelAttribute("coverRecipeForm") CoverRecipeForm coverForm,
            RedirectAttributes redirectAttributes) {

        User authUser = getAuthUser();
        if (authUser == null) return "redirect:/auth/login";

        if (coverForm.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "File gambar wajib dipilih");
            return "redirect:/recipes/" + coverForm.getId();
        }

        if (!coverForm.isValidImage()) {
            redirectAttributes.addFlashAttribute("error", "Format file harus JPG/PNG/GIF");
            return "redirect:/recipes/" + coverForm.getId();
        }

        try {
            // Upload File
            String fileName = fileStorageService.storeFile(coverForm.getCoverFile(), coverForm.getId());
            
            // Update DB
            recipeService.updateRecipeImage(authUser.getId(), coverForm.getId(), fileName);

            redirectAttributes.addFlashAttribute("success", "Foto cover berhasil diubah");
            return "redirect:/recipes/" + coverForm.getId();
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Gagal upload gambar");
            return "redirect:/recipes/" + coverForm.getId();
        }
    }

    // ========================================================================
    // SERVE IMAGE (Agar gambar bisa tampil di HTML)
    // ========================================================================
    @GetMapping("/cover/{filename:.+}")
    @ResponseBody
    public Resource getCoverByFilename(@PathVariable String filename) {
        try {
            Path file = fileStorageService.loadFile(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}