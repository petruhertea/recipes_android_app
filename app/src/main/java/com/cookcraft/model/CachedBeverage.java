package com.cookcraft.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cached_beverages")
public class CachedBeverage {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "recipe_id")
    private int recipeID;

    @ColumnInfo(name = "beverage_suggestions")
    private String beverageSuggestions;

    @ColumnInfo(name = "beverage_image")
    private String beverageImage;

    @ColumnInfo(name = "cached_timestamp")
    private long cachedTimestamp;

    public CachedBeverage(int recipeID, String beverageSuggestions,
                          String beverageImage, long cachedTimestamp) {
        this.recipeID = recipeID;
        this.beverageSuggestions = beverageSuggestions;
        this.beverageImage = beverageImage;
        this.cachedTimestamp = cachedTimestamp;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public long getCachedTimestamp() {
        return cachedTimestamp;
    }

    public void setCachedTimestamp(long cachedTimestamp) {
        this.cachedTimestamp = cachedTimestamp;
    }
}
