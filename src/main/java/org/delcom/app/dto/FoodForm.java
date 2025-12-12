package org.delcom.app.dto;

import java.util.UUID;

public class FoodForm {

    private UUID id;

    private String name;

    private Double calories;

    private Double protein;

    private Double carbohydrates;

    private Double fat;

    private Double fiber;

    private String servingSize;

    private String category;

    private String description;

    private String confirmName;

    // Constructor
    public FoodForm() {
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getCalories() {
        return calories;
    }

    public void setCalories(Double calories) {
        this.calories = calories;
    }

    public Double getProtein() {
        return protein;
    }

    public void setProtein(Double protein) {
        this.protein = protein;
    }

    public Double getCarbohydrates() {
        return carbohydrates;
    }

    public void setCarbohydrates(Double carbohydrates) {
        this.carbohydrates = carbohydrates;
    }

    public Double getFat() {
        return fat;
    }

    public void setFat(Double fat) {
        this.fat = fat;
    }

    public Double getFiber() {
        return fiber;
    }

    public void setFiber(Double fiber) {
        this.fiber = fiber;
    }

    public String getServingSize() {
        return servingSize;
    }

    public void setServingSize(String servingSize) {
        this.servingSize = servingSize;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConfirmName() {
        return confirmName;
    }

    public void setConfirmName(String confirmName) {
        this.confirmName = confirmName;
    }

    // Validation methods
    public boolean isValidNutritionValues() {
        return calories != null && calories >= 0 &&
               protein != null && protein >= 0 &&
               carbohydrates != null && carbohydrates >= 0 &&
               fat != null && fat >= 0 &&
               fiber != null && fiber >= 0;
    }

    public boolean isNameConfirmed() {
        return name != null && name.equals(confirmName);
    }
}