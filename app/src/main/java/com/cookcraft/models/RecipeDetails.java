package com.cookcraft.models;

import com.google.gson.annotations.SerializedName;

public class RecipeDetails {
    @SerializedName("recipeID")
    private int recipeID;

    @SerializedName("recipeTitle")
    private String recipeTitle;
    @SerializedName("recipeDescription")
    private String recipeDescription;
    @SerializedName("recipeInstructions")
    private String recipeInstructions;
    @SerializedName("servings")
    private int servings;
    @SerializedName("prep_time_minutes")
    private int prepTimeMinutes;
    @SerializedName("cook_time_minutes")
    private int cookTimeMinutes;
    @SerializedName("total_time_minutes")
    private int totalTimeMinutes;
    @SerializedName("recipeImage")
    private String recipeImage;
    @SerializedName("ingredients")
    private String ingredients;

    public int getRecipeID() {
        return recipeID;
    }

    public void setRecipeID(int recipeID) {
        this.recipeID = recipeID;
    }

    // Getters and setters

    public String getRecipeTitle() {
        return recipeTitle;
    }

    public void setRecipeTitle(String recipeTitle) {
        this.recipeTitle = recipeTitle;
    }

    public String getRecipeDescription() {
        return recipeDescription;
    }

    public void setRecipeDescription(String recipeDescription) {
        this.recipeDescription = recipeDescription;
    }

    public String getRecipeInstructions() {
        return recipeInstructions;
    }

    public void setRecipeInstructions(String recipeInstructions) {
        this.recipeInstructions = recipeInstructions;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public int getPrepTimeMinutes() {
        return prepTimeMinutes;
    }

    public void setPrepTimeMinutes(int prepTimeMinutes) {
        this.prepTimeMinutes = prepTimeMinutes;
    }

    public int getCookTimeMinutes() {
        return cookTimeMinutes;
    }

    public void setCookTimeMinutes(int cookTimeMinutes) {
        this.cookTimeMinutes = cookTimeMinutes;
    }

    public int getTotalTimeMinutes() {
        return totalTimeMinutes;
    }

    public void setTotalTimeMinutes(int totalTimeMinutes) {
        this.totalTimeMinutes = totalTimeMinutes;
    }

    public String getRecipeImage() {
        return recipeImage;
    }

    public void setRecipeImage(String recipeImage) {
        this.recipeImage = recipeImage;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }
}
