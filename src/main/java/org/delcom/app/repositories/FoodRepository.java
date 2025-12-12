package org.delcom.app.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.delcom.app.entities.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodRepository extends JpaRepository<Food, UUID> {
    
    @Query("SELECT f FROM Food f WHERE (LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(f.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(f.category) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND f.userId = :userId ORDER BY f.createdAt DESC")
    List<Food> findByKeyword(UUID userId, String keyword);

    @Query("SELECT f FROM Food f WHERE f.userId = :userId ORDER BY f.createdAt DESC")
    List<Food> findAllByUserId(UUID userId);

    @Query("SELECT f FROM Food f WHERE f.id = :id AND f.userId = :userId")
    Optional<Food> findByUserIdAndId(UUID userId, UUID id);

    @Query("SELECT f FROM Food f WHERE f.category = :category AND f.userId = :userId ORDER BY f.createdAt DESC")
    List<Food> findByUserIdAndCategory(UUID userId, String category);

    // Query untuk Chart Data - Total kalori per kategori
    @Query("SELECT f.category, SUM(f.calories) FROM Food f WHERE f.userId = :userId GROUP BY f.category")
    List<Object[]> getTotalCaloriesByCategory(UUID userId);

    // Query untuk Chart Data - Rata-rata nutrisi
    @Query("SELECT AVG(f.protein), AVG(f.carbohydrates), AVG(f.fat), AVG(f.fiber) FROM Food f WHERE f.userId = :userId")
    List<Object[]> getAverageNutrition(UUID userId);

    // Query untuk statistik - Total makanan per kategori
    @Query("SELECT f.category, COUNT(f) FROM Food f WHERE f.userId = :userId GROUP BY f.category")
    List<Object[]> getCountByCategory(UUID userId);
}

