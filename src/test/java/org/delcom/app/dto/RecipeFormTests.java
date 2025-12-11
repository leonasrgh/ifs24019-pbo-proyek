package org.delcom.app.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RecipeFormTests { // Ganti nama class

    private RecipeForm recipeForm; // Ganti nama variable

    @BeforeEach
    void setUp() {
        recipeForm = new RecipeForm();
    }

    @Test
    @DisplayName("Default constructor membuat objek dengan nilai default")
    void defaultConstructor_CreatesObjectWithDefaultValues() {
        assertNull(recipeForm.getId());
        assertNull(recipeForm.getTitle());
        assertNull(recipeForm.getDescription());
        assertNull(recipeForm.getIngredients()); // Cek Ingredients
        // assertNull(recipeForm.getConfirmTitle()); // Opsional jika field ini ada di DTO
    }

    @Test
    @DisplayName("Setter dan Getter untuk id bekerja dengan benar")
    void setterAndGetter_Id_WorksCorrectly() {
        UUID id = UUID.randomUUID();
        recipeForm.setId(id);
        assertEquals(id, recipeForm.getId());
    }

    @Test
    @DisplayName("Setter dan Getter untuk title bekerja dengan benar")
    void setterAndGetter_Title_WorksCorrectly() {
        String title = "Nasi Goreng";
        recipeForm.setTitle(title);
        assertEquals(title, recipeForm.getTitle());
    }

    @Test
    @DisplayName("Setter dan Getter untuk description bekerja dengan benar")
    void setterAndGetter_Description_WorksCorrectly() {
        String description = "Cara memasak nasi goreng...";
        recipeForm.setDescription(description);
        assertEquals(description, recipeForm.getDescription());
    }

    @Test
    @DisplayName("Setter dan Getter untuk ingredients bekerja dengan benar")
    void setterAndGetter_Ingredients_WorksCorrectly() {
        String ingredients = "Nasi, Telur, Kecap";
        recipeForm.setIngredients(ingredients);
        assertEquals(ingredients, recipeForm.getIngredients());
    }

    // Jika Anda masih menggunakan field confirmTitle di RecipeForm untuk fitur hapus:
    @Test
    @DisplayName("Setter dan Getter untuk confirmTitle bekerja dengan benar")
    void setterAndGetter_ConfirmTitle_WorksCorrectly() {
        String confirmTitle = "Konfirmasi Judul";
        recipeForm.setConfirmTitle(confirmTitle);
        assertEquals(confirmTitle, recipeForm.getConfirmTitle());
    }

    @Test
    @DisplayName("Semua field dapat diset dan diget dengan nilai berbagai tipe")
    void allFields_CanBeSetAndGet_WithVariousValues() {
        // Arrange
        UUID id = UUID.randomUUID();
        String title = "Sate Ayam";
        String description = "Panggang sate...";
        String ingredients = "Daging ayam, bumbu kacang";
        String confirmTitle = "CONFIRM";

        // Act
        recipeForm.setId(id);
        recipeForm.setTitle(title);
        recipeForm.setDescription(description);
        recipeForm.setIngredients(ingredients);
        recipeForm.setConfirmTitle(confirmTitle);

        // Assert
        assertEquals(id, recipeForm.getId());
        assertEquals(title, recipeForm.getTitle());
        assertEquals(description, recipeForm.getDescription());
        assertEquals(ingredients, recipeForm.getIngredients());
        assertEquals(confirmTitle, recipeForm.getConfirmTitle());
    }
}