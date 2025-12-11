package org.delcom.app.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RecipeTests {

    @Test
    @DisplayName("Membuat instance dari kelas Recipe dengan benar")
    void testMembuatInstanceRecipe() {
        UUID userId = UUID.randomUUID();

        // 1. Test Konstruktor dengan Parameter
        {
            // Konstruktor baru: userId, title, description, ingredients
            Recipe recipe = new Recipe(userId, "Nasi Goreng", "Goreng nasi", "Nasi, Kecap, Telur");

            assertEquals(userId, recipe.getUserId());
            assertEquals("Nasi Goreng", recipe.getTitle());
            assertEquals("Goreng nasi", recipe.getDescription());
            assertEquals("Nasi, Kecap, Telur", recipe.getIngredients());
            assertNull(recipe.getCover()); // Cover default null
        }

        // 2. Test Konstruktor Default (Kosong)
        {
            Recipe recipe = new Recipe();

            assertNull(recipe.getId());
            assertNull(recipe.getUserId());
            assertNull(recipe.getTitle());
            assertNull(recipe.getDescription());
            assertNull(recipe.getIngredients());
        }

        // 3. Test Setter dan Getter
        {
            Recipe recipe = new Recipe();
            UUID generatedId = UUID.randomUUID();
            
            recipe.setId(generatedId);
            recipe.setUserId(userId);
            recipe.setTitle("Set Title");
            recipe.setDescription("Set Description");
            recipe.setIngredients("Set Ingredients");
            recipe.setCover("gambar.jpg");
            
            // Simulasi Lifecycle JPA
            recipe.onCreate(); 
            recipe.onUpdate();

            assertEquals(generatedId, recipe.getId());
            assertEquals(userId, recipe.getUserId());
            assertEquals("Set Title", recipe.getTitle());
            assertEquals("Set Description", recipe.getDescription());
            assertEquals("Set Ingredients", recipe.getIngredients());
            assertEquals("gambar.jpg", recipe.getCover());
            
            assertNotNull(recipe.getCreatedAt());
            assertNotNull(recipe.getUpdatedAt());
        }
    }
}