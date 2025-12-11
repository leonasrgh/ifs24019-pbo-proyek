package org.delcom.app.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;

public class RecipeForm {

    private UUID id;

    @NotBlank(message = "Judul resep tidak boleh kosong")
    private String title;

    @NotBlank(message = "Deskripsi resep tidak boleh kosong")
    private String description;

    @NotBlank(message = "Bahan-bahan tidak boleh kosong")
    private String ingredients;

    private String confirmTitle; // Untuk konfirmasi saat delete (opsional, mirip TodoForm)

    // Constructor
    public RecipeForm() {
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getConfirmTitle() {
        return confirmTitle;
    }

    public void setConfirmTitle(String confirmTitle) {
        this.confirmTitle = confirmTitle;
    }
}