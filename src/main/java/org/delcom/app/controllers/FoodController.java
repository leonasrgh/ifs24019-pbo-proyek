package org.delcom.app.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.dto.CoverFoodForm;
import org.delcom.app.entities.Food;
import org.delcom.app.entities.User;
import org.delcom.app.services.FileStorageService;
import org.delcom.app.services.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/foods")
public class FoodController {
    private final FoodService foodService;
    private final FileStorageService fileStorageService;

    @Autowired
    protected AuthContext authContext;

    public FoodController(FoodService foodService, FileStorageService fileStorageService) {
        this.foodService = foodService;
        this.fileStorageService = fileStorageService;
    }

    // Menambahkan food baru
    // -------------------------------
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, UUID>>> createFood(@RequestBody Food reqFood) {

        // Validasi input
        if (reqFood.getName() == null || reqFood.getName().isEmpty()) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("fail", "Data name tidak valid", null));
        }
        if (reqFood.getCalories() == null || reqFood.getCalories() < 0) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("fail", "Data calories tidak valid", null));
        }
        if (reqFood.getProtein() == null || reqFood.getProtein() < 0) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("fail", "Data protein tidak valid", null));
        }
        if (reqFood.getCarbohydrates() == null || reqFood.getCarbohydrates() < 0) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("fail", "Data carbohydrates tidak valid", null));
        }
        if (reqFood.getFat() == null || reqFood.getFat() < 0) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("fail", "Data fat tidak valid", null));
        }
        if (reqFood.getFiber() == null || reqFood.getFiber() < 0) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("fail", "Data fiber tidak valid", null));
        }
        if (reqFood.getServingSize() == null || reqFood.getServingSize().isEmpty()) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("fail", "Data serving size tidak valid", null));
        }
        if (reqFood.getCategory() == null || reqFood.getCategory().isEmpty()) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("fail", "Data category tidak valid", null));
        }

        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(
                new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        Food newFood = foodService.createFood(
            authUser.getId(),
            reqFood.getName(),
            reqFood.getCalories(),
            reqFood.getProtein(),
            reqFood.getCarbohydrates(),
            reqFood.getFat(),
            reqFood.getFiber(),
            reqFood.getServingSize(),
            reqFood.getCategory(),
            reqFood.getDescription()
        );

        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Food berhasil dibuat",
            Map.of("id", newFood.getId())
        ));
    }

    // Mendapatkan semua food dengan opsi pencarian
    // -------------------------------
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<Food>>>> getAllFoods(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {
        
        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(
                new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        List<Food> foods;
        if (category != null && !category.trim().isEmpty()) {
            foods = foodService.getFoodsByCategory(authUser.getId(), category);
        } else {
            foods = foodService.getAllFoods(authUser.getId(), search);
        }

        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Daftar food berhasil diambil",
            Map.of("foods", foods)
        ));
    }

    // Mendapatkan food berdasarkan ID
    // -------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Food>>> getFoodById(@PathVariable UUID id) {
        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(
                new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        Food food = foodService.getFoodById(authUser.getId(), id);
        if (food == null) {
            return ResponseEntity.status(404).body(
                new ApiResponse<>("fail", "Data food tidak ditemukan", null));
        }

        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Data food berhasil diambil",
            Map.of("food", food)
        ));
    }

    // Memperbarui food berdasarkan ID
    // -------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Food>> updateFood(
            @PathVariable UUID id, 
            @RequestBody Food reqFood) {

        // Validasi input
        if (reqFood.getName() == null || reqFood.getName().isEmpty()) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("fail", "Data name tidak valid", null));
        }
        if (reqFood.getCalories() == null || reqFood.getCalories() < 0) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("fail", "Data calories tidak valid", null));
        }
        if (reqFood.getProtein() == null || reqFood.getProtein() < 0) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("fail", "Data protein tidak valid", null));
        }
        if (reqFood.getCarbohydrates() == null || reqFood.getCarbohydrates() < 0) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("fail", "Data carbohydrates tidak valid", null));
        }
        if (reqFood.getFat() == null || reqFood.getFat() < 0) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("fail", "Data fat tidak valid", null));
        }
        if (reqFood.getFiber() == null || reqFood.getFiber() < 0) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("fail", "Data fiber tidak valid", null));
        }
        if (reqFood.getServingSize() == null || reqFood.getServingSize().isEmpty()) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("fail", "Data serving size tidak valid", null));
        }
        if (reqFood.getCategory() == null || reqFood.getCategory().isEmpty()) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("fail", "Data category tidak valid", null));
        }

        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(
                new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        Food updatedFood = foodService.updateFood(
            authUser.getId(),
            id,
            reqFood.getName(),
            reqFood.getCalories(),
            reqFood.getProtein(),
            reqFood.getCarbohydrates(),
            reqFood.getFat(),
            reqFood.getFiber(),
            reqFood.getServingSize(),
            reqFood.getCategory(),
            reqFood.getDescription()
        );

        if (updatedFood == null) {
            return ResponseEntity.status(404).body(
                new ApiResponse<>("fail", "Data food tidak ditemukan", null));
        }

        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Data food berhasil diperbarui",
            null
        ));
    }

    // Menghapus food berdasarkan ID
    // -------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteFood(@PathVariable UUID id) {
        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(
                new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        boolean status = foodService.deleteFood(authUser.getId(), id);
        if (!status) {
            return ResponseEntity.status(404).body(
                new ApiResponse<>("fail", "Data food tidak ditemukan", null));
        }

        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Data food berhasil dihapus",
            null
        ));
    }

    // Mendapatkan statistik nutrisi untuk chart
    // -------------------------------
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNutritionStatistics() {
        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(
                new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        Map<String, Object> statistics = foodService.getNutritionStatistics(authUser.getId());

        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Statistik nutrisi berhasil diambil",
            statistics
        ));
    }

    // Upload/Update Food Cover
    // -------------------------------
    @PostMapping("/{id}/cover")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFoodCover(
            @PathVariable UUID id,
            @ModelAttribute CoverFoodForm coverFoodForm) {

        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(
                new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        // Validasi file
        String validationError = coverFoodForm.getValidationError();
        if (validationError != null) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("fail", validationError, null));
        }

        // Cek apakah food exists
        Food food = foodService.getFoodById(authUser.getId(), id);
        if (food == null) {
            return ResponseEntity.status(404).body(
                new ApiResponse<>("fail", "Data food tidak ditemukan", null));
        }

        try {
            // Simpan file
            String filename = fileStorageService.storeFile(
                coverFoodForm.getCoverFile(), id);

            // Update database
            foodService.updateCover(authUser.getId(), id, filename);

            return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Cover food berhasil diupload",
                Map.of("cover", filename)
            ));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(
                new ApiResponse<>("fail", "Gagal mengupload cover: " + e.getMessage(), null));
        }
    }

    // Get Food Cover/Image
    // -------------------------------
    @GetMapping("/covers/{filename:.+}")
    public ResponseEntity<Resource> getFoodCover(@PathVariable String filename) {
        try {
            Path filePath = fileStorageService.loadFile(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete Food Cover
    // -------------------------------
    @DeleteMapping("/{id}/cover")
    public ResponseEntity<ApiResponse<String>> deleteFoodCover(@PathVariable UUID id) {
        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(
                new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        Food food = foodService.getFoodById(authUser.getId(), id);
        if (food == null) {
            return ResponseEntity.status(404).body(
                new ApiResponse<>("fail", "Data food tidak ditemukan", null));
        }

        if (food.getCover() == null) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("fail", "Food tidak memiliki cover", null));
        }

        // Hapus file
        fileStorageService.deleteFile(food.getCover());

        // Update database
        foodService.updateCover(authUser.getId(), id, null);

        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Cover food berhasil dihapus",
            null
        ));
    }
}