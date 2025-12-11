package org.delcom.app.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.delcom.app.entities.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, UUID> {

    // Mencari resep berdasarkan Keyword (Judul, Deskripsi, atau Bahan)
    // Menambahkan pencarian ke 'ingredients' agar lebih powerfull
    @Query("SELECT r FROM Recipe r WHERE (LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(r.ingredients) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND r.userId = :userId ORDER BY r.createdAt DESC")
    List<Recipe> findByKeyword(UUID userId, String keyword);

    // Mengambil semua resep milik user tertentu (Diurutkan yang terbaru)
    @Query("SELECT r FROM Recipe r WHERE r.userId = :userId ORDER BY r.createdAt DESC")
    List<Recipe> findAllByUserId(UUID userId);

    // Mengambil satu resep spesifik dan memastikan milik user yang login
    @Query("SELECT r FROM Recipe r WHERE r.id = :id AND r.userId = :userId")
    Optional<Recipe> findByUserIdAndId(UUID userId, UUID id);
    
    // (Opsional) Untuk menghitung statistik data chart
    @Query("SELECT COUNT(r) FROM Recipe r WHERE r.userId = :userId")
    long countByUserId(UUID userId);
}