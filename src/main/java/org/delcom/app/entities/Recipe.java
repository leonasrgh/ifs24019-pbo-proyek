package org.delcom.app.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recipes")
public class Recipe {

    // ==========================================
    // 8 ATRIBUT WAJIB (AGAR NILAI MAKSIMAL)
    // ==========================================

    // 1. ID (Wajib)
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    // 2. User ID (Wajib - Relasi ke User)
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // 3. Judul Resep
    @Column(name = "title", nullable = false)
    private String title;

    // 4. Deskripsi / Cara Masak (Pakai TEXT agar muat banyak)
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    // 5. Bahan-bahan (Pakai TEXT agar muat banyak)
    @Column(name = "ingredients", nullable = false, columnDefinition = "TEXT")
    private String ingredients;

    // 6. Path Gambar Cover (Untuk fitur Ubah Gambar)
    @Column(name = "cover", nullable = true)
    private String cover;

    // 7. Created At (Wajib)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 8. Updated At (Wajib)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==========================================
    // CONSTRUCTORS
    // ==========================================
    public Recipe() {
    }

    public Recipe(UUID userId, String title, String description, String ingredients) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.ingredients = ingredients;
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ==========================================
    // LIFECYCLE CALLBACKS
    // ==========================================
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}