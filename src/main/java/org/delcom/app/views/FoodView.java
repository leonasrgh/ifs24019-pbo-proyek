package org.delcom.app.views;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.delcom.app.dto.CoverFoodForm;
import org.delcom.app.dto.FoodForm;
import org.delcom.app.entities.Food;
import org.delcom.app.entities.User;
import org.delcom.app.services.FileStorageService;
import org.delcom.app.services.FoodService;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/foods")
public class FoodView {

    private final FoodService foodService;
    private final FileStorageService fileStorageService;

    public FoodView(FoodService foodService, FileStorageService fileStorageService) {
        this.foodService = foodService;
        this.fileStorageService = fileStorageService;
    }

    // Halaman list foods
    @GetMapping
    public String getFoodList(@RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            HttpServletRequest request,
            Model model) {
        
        // Autentikasi user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/login";
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/login";
        }
        User authUser = (User) principal;
        model.addAttribute("auth", authUser);

        // TAMBAHKAN: currentPath untuk navbar
        model.addAttribute("currentPath", request.getRequestURI());

        // Ambil foods
        var foods = foodService.getAllFoods(authUser.getId(), search);
        if (category != null && !category.isBlank()) {
            foods = foodService.getFoodsByCategory(authUser.getId(), category);
        }
        model.addAttribute("foods", foods);
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedCategory", category);

        // Food Form untuk modal add
        model.addAttribute("foodForm", new FoodForm());

        return ConstUtil.TEMPLATE_PAGES_FOODS_LIST;
    }

    @PostMapping("/add")
    public String postAddFood(@Valid @ModelAttribute("foodForm") FoodForm foodForm,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {

        // Autentikasi user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/login";
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/login";
        }
        User authUser = (User) principal;

        // Validasi form
        if (foodForm.getName() == null || foodForm.getName().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Nama makanan tidak boleh kosong");
            redirectAttributes.addFlashAttribute("addFoodModalOpen", true);
            return "redirect:/foods";
        }

        if (!foodForm.isValidNutritionValues()) {
            redirectAttributes.addFlashAttribute("error", "Nilai nutrisi tidak valid");
            redirectAttributes.addFlashAttribute("addFoodModalOpen", true);
            return "redirect:/foods";
        }

        if (foodForm.getServingSize() == null || foodForm.getServingSize().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Ukuran porsi tidak boleh kosong");
            redirectAttributes.addFlashAttribute("addFoodModalOpen", true);
            return "redirect:/foods";
        }

        if (foodForm.getCategory() == null || foodForm.getCategory().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Kategori tidak boleh kosong");
            redirectAttributes.addFlashAttribute("addFoodModalOpen", true);
            return "redirect:/foods";
        }

        // Simpan food
        var entity = foodService.createFood(
                authUser.getId(),
                foodForm.getName(),
                foodForm.getCalories(),
                foodForm.getProtein(),
                foodForm.getCarbohydrates(),
                foodForm.getFat(),
                foodForm.getFiber(),
                foodForm.getServingSize(),
                foodForm.getCategory(),
                foodForm.getDescription());

        if (entity == null) {
            redirectAttributes.addFlashAttribute("error", "Gagal menambahkan makanan");
            redirectAttributes.addFlashAttribute("addFoodModalOpen", true);
            return "redirect:/foods";
        }

        // Redirect dengan pesan sukses
        redirectAttributes.addFlashAttribute("success", "Makanan berhasil ditambahkan.");
        return "redirect:/foods";
    }

    @PostMapping("/edit")
    public String postEditFood(@Valid @ModelAttribute("foodForm") FoodForm foodForm,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {
        
        // Autentikasi user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/login";
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/login";
        }
        User authUser = (User) principal;

        // Validasi form
        if (foodForm.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "ID makanan tidak valid");
            redirectAttributes.addFlashAttribute("editFoodModalOpen", true);
            return "redirect:/foods";
        }

        if (foodForm.getName() == null || foodForm.getName().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Nama makanan tidak boleh kosong");
            redirectAttributes.addFlashAttribute("editFoodModalOpen", true);
            redirectAttributes.addFlashAttribute("editFoodModalId", foodForm.getId());
            return "redirect:/foods/" + foodForm.getId();
        }

        if (!foodForm.isValidNutritionValues()) {
            redirectAttributes.addFlashAttribute("error", "Nilai nutrisi tidak valid");
            redirectAttributes.addFlashAttribute("editFoodModalOpen", true);
            redirectAttributes.addFlashAttribute("editFoodModalId", foodForm.getId());
            return "redirect:/foods/" + foodForm.getId();
        }

        // Update food
        var updated = foodService.updateFood(
                authUser.getId(),
                foodForm.getId(),
                foodForm.getName(),
                foodForm.getCalories(),
                foodForm.getProtein(),
                foodForm.getCarbohydrates(),
                foodForm.getFat(),
                foodForm.getFiber(),
                foodForm.getServingSize(),
                foodForm.getCategory(),
                foodForm.getDescription());

        if (updated == null) {
            redirectAttributes.addFlashAttribute("error", "Gagal memperbarui makanan");
            redirectAttributes.addFlashAttribute("editFoodModalOpen", true);
            redirectAttributes.addFlashAttribute("editFoodModalId", foodForm.getId());
            return "redirect:/foods/" + foodForm.getId();
        }

        // Redirect dengan pesan sukses
        redirectAttributes.addFlashAttribute("success", "Makanan berhasil diperbarui.");
        return "redirect:/foods/" + foodForm.getId();
    }

    @PostMapping("/delete")
    public String postDeleteFood(@Valid @ModelAttribute("foodForm") FoodForm foodForm,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {

        // Autentikasi user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/login";
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/login";
        }
        User authUser = (User) principal;

        // Validasi form
        if (foodForm.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "ID makanan tidak valid");
            redirectAttributes.addFlashAttribute("deleteFoodModalOpen", true);
            return "redirect:/foods";
        }

        if (foodForm.getConfirmName() == null || foodForm.getConfirmName().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Konfirmasi nama tidak boleh kosong");
            redirectAttributes.addFlashAttribute("deleteFoodModalOpen", true);
            redirectAttributes.addFlashAttribute("deleteFoodModalId", foodForm.getId());
            return "redirect:/foods/" + foodForm.getId();
        }

        // Periksa apakah food tersedia
        Food existingFood = foodService.getFoodById(authUser.getId(), foodForm.getId());
        if (existingFood == null) {
            redirectAttributes.addFlashAttribute("error", "Makanan tidak ditemukan");
            redirectAttributes.addFlashAttribute("deleteFoodModalOpen", true);
            redirectAttributes.addFlashAttribute("deleteFoodModalId", foodForm.getId());
            return "redirect:/foods";
        }

        if (!existingFood.getName().equals(foodForm.getConfirmName())) {
            redirectAttributes.addFlashAttribute("error", "Konfirmasi nama tidak sesuai");
            redirectAttributes.addFlashAttribute("deleteFoodModalOpen", true);
            redirectAttributes.addFlashAttribute("deleteFoodModalId", foodForm.getId());
            return "redirect:/foods/" + foodForm.getId();
        }

        // Hapus food
        boolean deleted = foodService.deleteFood(
                authUser.getId(),
                foodForm.getId());
        if (!deleted) {
            redirectAttributes.addFlashAttribute("error", "Gagal menghapus makanan");
            redirectAttributes.addFlashAttribute("deleteFoodModalOpen", true);
            redirectAttributes.addFlashAttribute("deleteFoodModalId", foodForm.getId());
            return "redirect:/foods/" + foodForm.getId();
        }

        // Redirect dengan pesan sukses
        redirectAttributes.addFlashAttribute("success", "Makanan berhasil dihapus.");
        return "redirect:/foods";
    }

    @GetMapping("/{foodId}")
    public String getDetailFood(@PathVariable UUID foodId, 
                                HttpServletRequest request,
                                Model model) {
        // Autentikasi user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/login";
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/login";
        }
        User authUser = (User) principal;
        model.addAttribute("auth", authUser);

        // TAMBAHKAN: currentPath untuk navbar
        model.addAttribute("currentPath", request.getRequestURI());

        // Ambil food
        Food food = foodService.getFoodById(authUser.getId(), foodId);
        if (food == null) {
            return "redirect:/foods";
        }
        model.addAttribute("food", food);

        // Cover Food Form
        CoverFoodForm coverFoodForm = new CoverFoodForm();
        coverFoodForm.setId(foodId);
        model.addAttribute("coverFoodForm", coverFoodForm);

        // Food Form untuk edit
        FoodForm foodForm = new FoodForm();
        foodForm.setId(food.getId());
        foodForm.setName(food.getName());
        foodForm.setCalories(food.getCalories());
        foodForm.setProtein(food.getProtein());
        foodForm.setCarbohydrates(food.getCarbohydrates());
        foodForm.setFat(food.getFat());
        foodForm.setFiber(food.getFiber());
        foodForm.setServingSize(food.getServingSize());
        foodForm.setCategory(food.getCategory());
        foodForm.setDescription(food.getDescription());
        model.addAttribute("foodForm", foodForm);

        return ConstUtil.TEMPLATE_PAGES_FOODS_DETAIL;
    }

    @PostMapping("/edit-cover")
    public String postEditCoverFood(@Valid @ModelAttribute("coverFoodForm") CoverFoodForm coverFoodForm,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {

        // Autentikasi user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/login";
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/login";
        }
        User authUser = (User) principal;

        if (coverFoodForm.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "File cover tidak boleh kosong");
            redirectAttributes.addFlashAttribute("editCoverFoodModalOpen", true);
            return "redirect:/foods/" + coverFoodForm.getId();
        }

        // Check if food exists
        Food food = foodService.getFoodById(authUser.getId(), coverFoodForm.getId());
        if (food == null) {
            redirectAttributes.addFlashAttribute("error", "Makanan tidak ditemukan");
            redirectAttributes.addFlashAttribute("editCoverFoodModalOpen", true);
            return "redirect:/foods";
        }

        // Validasi manual file type
        if (!coverFoodForm.isValidImage()) {
            redirectAttributes.addFlashAttribute("error", "Format file tidak didukung. Gunakan JPG, PNG, atau GIF");
            redirectAttributes.addFlashAttribute("editCoverFoodModalOpen", true);
            return "redirect:/foods/" + coverFoodForm.getId();
        }

        // Validasi file size (max 5MB)
        if (!coverFoodForm.isSizeValid(5 * 1024 * 1024)) {
            redirectAttributes.addFlashAttribute("error", "Ukuran file terlalu besar. Maksimal 5MB");
            redirectAttributes.addFlashAttribute("editCoverFoodModalOpen", true);
            return "redirect:/foods/" + coverFoodForm.getId();
        }

        try {
            // Simpan file
            String fileName = fileStorageService.storeFile(coverFoodForm.getCoverFile(), coverFoodForm.getId());

            // Update food dengan nama file cover
            foodService.updateCover(authUser.getId(), coverFoodForm.getId(), fileName);

            redirectAttributes.addFlashAttribute("success", "Cover berhasil diupload");
            return "redirect:/foods/" + coverFoodForm.getId();
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Gagal mengupload cover");
            redirectAttributes.addFlashAttribute("editCoverFoodModalOpen", true);
            return "redirect:/foods/" + coverFoodForm.getId();
        }
    }

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

    // Halaman Statistics/Chart
    @GetMapping("/statistics")
    public String getStatistics(HttpServletRequest request, Model model) {
        // Autentikasi user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/login";
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/login";
        }
        User authUser = (User) principal;
        model.addAttribute("auth", authUser);

        // TAMBAHKAN: currentPath untuk navbar
        model.addAttribute("currentPath", request.getRequestURI());

        // Ambil foods dulu
        var foods = foodService.getAllFoods(authUser.getId(), "");
        model.addAttribute("foods", foods);

        // Ambil statistics dengan fallback
        Map<String, Object> statistics;
        try {
            statistics = foodService.getNutritionStatistics(authUser.getId());
            if (statistics == null) {
                statistics = createEmptyStatistics();
            }
        } catch (Exception e) {
            statistics = createEmptyStatistics();
        }
        model.addAttribute("statistics", statistics);

        return ConstUtil.TEMPLATE_PAGES_FOODS_STATISTICS;
    }

    // Helper method untuk create empty statistics
    private Map<String, Object> createEmptyStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("caloriesByCategory", new HashMap<String, Double>());
        stats.put("averageNutrition", new HashMap<String, Double>());
        stats.put("countByCategory", new HashMap<String, Long>());
        return stats;
    }
}