package com.recipes.models;

import com.google.gson.annotations.SerializedName;

public class BeverageDetails {
    @SerializedName("recipeID")
    private int recipeID;
    @SerializedName("beverageSuggestions")
    private String beverageSuggestions;
    @SerializedName("beverageImage")
    private String beverageImage;

    public int getRecipeID() {
        return recipeID;
    }

    public void setRecipeID(int recipeID) {
        this.recipeID = recipeID;
    }

    public String getBeverageSuggestions() {
        return beverageSuggestions;
    }

    public void setBeverageSuggestions(String beverageSuggestions) {
        this.beverageSuggestions = beverageSuggestions;
    }

    public String getBeverageImage() {
        return beverageImage;
    }

    public void setBeverageImage(String beverageImage) {
        this.beverageImage = beverageImage;
    }
}
