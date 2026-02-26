package com.cookcraft.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity for the offline recipe cache.
 *
 * The structured ingredients list from the API is serialised to a compact
 * CSV string ("Făină: 500.0 g, Ouă: 3.0 buc.") for storage, then rebuilt
 * into {@link RecipeDetails.IngredientLine} objects when the cache is read.
 * See {@link RecipeDetails#ingredientsToString()} and
 * {@link RecipeDetails#ingredientsFromString(String)}.
 */
@Entity(tableName = "cached_recipes")
public class CachedRecipe {

    @PrimaryKey
    private int recipeID;

    @ColumnInfo(name = "recipe_title")
    private String recipeTitle;

    @ColumnInfo(name = "recipe_description")
    private String recipeDescription;

    @ColumnInfo(name = "recipe_instructions")
    private String recipeInstructions;

    private int servings;

    @ColumnInfo(name = "prep_time_minutes")
    private int prepTimeMinutes;

    @ColumnInfo(name = "cook_time_minutes")
    private int cookTimeMinutes;

    @ColumnInfo(name = "total_time_minutes")
    private int totalTimeMinutes;

    @ColumnInfo(name = "recipe_image")
    private String recipeImage;

    /** Flat serialised form of List<IngredientLine>: "Name: qty unit, ..." */
    private String ingredients;

    @ColumnInfo(name = "cached_timestamp")
    private long cachedTimestamp;

    public CachedRecipe(int recipeID, String recipeTitle, String recipeDescription,
                        String recipeInstructions, int servings, int prepTimeMinutes,
                        int cookTimeMinutes, int totalTimeMinutes, String recipeImage,
                        String ingredients, long cachedTimestamp) {
        this.recipeID = recipeID;
        this.recipeTitle = recipeTitle;
        this.recipeDescription = recipeDescription;
        this.recipeInstructions = recipeInstructions;
        this.servings = servings;
        this.prepTimeMinutes = prepTimeMinutes;
        this.cookTimeMinutes = cookTimeMinutes;
        this.totalTimeMinutes = totalTimeMinutes;
        this.recipeImage = recipeImage;
        this.ingredients = ingredients;
        this.cachedTimestamp = cachedTimestamp;
    }

    // ─── Getters & Setters ───────────────────────────────────────────────

    public int getRecipeID()                          { return recipeID; }
    public void setRecipeID(int recipeID)             { this.recipeID = recipeID; }

    public String getRecipeTitle()                    { return recipeTitle; }
    public void setRecipeTitle(String recipeTitle)    { this.recipeTitle = recipeTitle; }

    public String getRecipeDescription()              { return recipeDescription; }
    public void setRecipeDescription(String d)        { this.recipeDescription = d; }

    public String getRecipeInstructions()             { return recipeInstructions; }
    public void setRecipeInstructions(String i)       { this.recipeInstructions = i; }

    public int getServings()                          { return servings; }
    public void setServings(int servings)             { this.servings = servings; }

    public int getPrepTimeMinutes()                   { return prepTimeMinutes; }
    public void setPrepTimeMinutes(int v)             { this.prepTimeMinutes = v; }

    public int getCookTimeMinutes()                   { return cookTimeMinutes; }
    public void setCookTimeMinutes(int v)             { this.cookTimeMinutes = v; }

    public int getTotalTimeMinutes()                  { return totalTimeMinutes; }
    public void setTotalTimeMinutes(int v)            { this.totalTimeMinutes = v; }

    public String getRecipeImage()                    { return recipeImage; }
    public void setRecipeImage(String recipeImage)    { this.recipeImage = recipeImage; }

    public String getIngredients()                    { return ingredients; }
    public void setIngredients(String ingredients)    { this.ingredients = ingredients; }

    public long getCachedTimestamp()                  { return cachedTimestamp; }
    public void setCachedTimestamp(long ts)           { this.cachedTimestamp = ts; }
}