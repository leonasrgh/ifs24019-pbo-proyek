package org.delcom.app.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

import org.delcom.app.entities.Food;
import org.delcom.app.repositories.FoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FoodService {
    private final FoodRepository foodRepository;
    private final FileStorageService fileStorageService;

    public FoodService(FoodRepository foodRepository, FileStorageService fileStorageService) {
        this.foodRepository = foodRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public Food createFood(UUID userId, String name, Double calories, Double protein,
                          Double carbohydrates, Double fat, Double fiber,
                          String servingSize, String category, String description) {
        Food food = new Food(userId, name, calories, protein, carbohydrates, 
                           fat, fiber, servingSize, category, description);
        return foodRepository.save(food);
    }

    public List<Food> getAllFoods(UUID userId, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return foodRepository.findByKeyword(userId, search);
        }
        return foodRepository.findAllByUserId(userId);
    }

    public List<Food> getFoodsByCategory(UUID userId, String category) {
        return foodRepository.findByUserIdAndCategory(userId, category);
    }

    public Food getFoodById(UUID userId, UUID id) {
        return foodRepository.findByUserIdAndId(userId, id).orElse(null);
    }

    @Transactional
    public Food updateFood(UUID userId, UUID id, String name, Double calories,
                          Double protein, Double carbohydrates, Double fat,
                          Double fiber, String servingSize, String category,
                          String description) {
        Food food = foodRepository.findByUserIdAndId(userId, id).orElse(null);
        if (food != null) {
            food.setName(name);
            food.setCalories(calories);
            food.setProtein(protein);
            food.setCarbohydrates(carbohydrates);
            food.setFat(fat);
            food.setFiber(fiber);
            food.setServingSize(servingSize);
            food.setCategory(category);
            food.setDescription(description);
            return foodRepository.save(food);
        }
        return null;
    }

    @Transactional
    public boolean deleteFood(UUID userId, UUID id) {
        Food food = foodRepository.findByUserIdAndId(userId, id).orElse(null);
        if (food == null) {
            return false;
        }

        // Hapus cover jika ada
        if (food.getCover() != null) {
            fileStorageService.deleteFile(food.getCover());
        }

        foodRepository.deleteById(id);
        return true;
    }

    @Transactional
    public Food updateCover(UUID userId, UUID foodId, String coverFilename) {
        Optional<Food> foodOpt = foodRepository.findByUserIdAndId(userId, foodId);
        if (foodOpt.isPresent()) {
            Food food = foodOpt.get();

            // Hapus file cover lama jika ada
            if (food.getCover() != null) {
                fileStorageService.deleteFile(food.getCover());
            }

            food.setCover(coverFilename);
            return foodRepository.save(food);
        }
        return null;
    }

    // Method untuk Chart Data
    public Map<String, Object> getNutritionStatistics(UUID userId) {
        Map<String, Object> statistics = new HashMap<>();

        // Total kalori per kategori
        List<Object[]> caloriesByCategory = foodRepository.getTotalCaloriesByCategory(userId);
        Map<String, Double> caloriesMap = new HashMap<>();
        for (Object[] result : caloriesByCategory) {
            caloriesMap.put((String) result[0], (Double) result[1]);
        }
        statistics.put("caloriesByCategory", caloriesMap);

        // Rata-rata nutrisi
        List<Object[]> avgNutrition = foodRepository.getAverageNutrition(userId);
        if (!avgNutrition.isEmpty() && avgNutrition.get(0)[0] != null) {
            Map<String, Double> avgMap = new HashMap<>();
            avgMap.put("protein", (Double) avgNutrition.get(0)[0]);
            avgMap.put("carbohydrates", (Double) avgNutrition.get(0)[1]);
            avgMap.put("fat", (Double) avgNutrition.get(0)[2]);
            avgMap.put("fiber", (Double) avgNutrition.get(0)[3]);
            statistics.put("averageNutrition", avgMap);
        }

        // Jumlah makanan per kategori
        List<Object[]> countByCategory = foodRepository.getCountByCategory(userId);
        Map<String, Long> countMap = new HashMap<>();
        for (Object[] result : countByCategory) {
            countMap.put((String) result[0], (Long) result[1]);
        }
        statistics.put("countByCategory", countMap);

        return statistics;
    }
}