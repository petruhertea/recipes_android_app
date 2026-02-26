package com.cookcraft.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * API response model for a single recipe.
 *
 * Matches the backend RecipeDTO shape:
 *   - "id"               → recipeID
 *   - "title"            → recipeTitle
 *   - "description"      → recipeDescription
 *   - "instructions"     → recipeInstructions
 *   - "ingredients"      → List<IngredientLine>  (structured, no longer a CSV string)
 *   - time/servings fields → unchanged
 */
public class RecipeDetails {

    @SerializedName("id")
    private int recipeID;

    @SerializedName("title")
    private String recipeTitle;

    @SerializedName("description")
    private String recipeDescription;

    @SerializedName("instructions")
    private String recipeInstructions;

    @SerializedName("servings")
    private int servings;

    @SerializedName("prepTimeMinutes")
    private int prepTimeMinutes;

    @SerializedName("cookTimeMinutes")
    private int cookTimeMinutes;

    @SerializedName("totalTimeMinutes")
    private int totalTimeMinutes;

    @SerializedName("recipeImage")
    private String recipeImage;

    /** Structured ingredient list – replaces the old flat CSV string. */
    @SerializedName("ingredients")
    private List<IngredientLine> ingredients = new ArrayList<>();

    // ─── Nested DTO ──────────────────────────────────────────────────────

    /**
     * One ingredient line as returned by the API.
     * Maps to RecipeDTO.IngredientLineDTO on the backend.
     */
    public static class IngredientLine {
        @SerializedName("name")
        private String name;

        @SerializedName("quantity")
        private double quantity;

        @SerializedName("unit")
        private String unit;

        public IngredientLine() {}

        public IngredientLine(String name, double quantity, String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }

        public String getName()     { return name; }
        public void setName(String name) { this.name = name; }

        public double getQuantity() { return quantity; }
        public void setQuantity(double quantity) { this.quantity = quantity; }

        public String getUnit()     { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        /**
         * Human-readable form used when building display strings or
         * serialising to the Room cache: "Făină: 500.0 g"
         */
        @Override
        public String toString() {
            if (unit != null && !unit.isBlank()) {
                return name + ": " + quantity + " " + unit;
            }
            return name + ": " + quantity;
        }
    }

    // ─── Getters & Setters ───────────────────────────────────────────────

    public int getRecipeID()                        { return recipeID; }
    public void setRecipeID(int recipeID)           { this.recipeID = recipeID; }

    public String getRecipeTitle()                  { return recipeTitle; }
    public void setRecipeTitle(String recipeTitle)  { this.recipeTitle = recipeTitle; }

    public String getRecipeDescription()            { return recipeDescription; }
    public void setRecipeDescription(String d)      { this.recipeDescription = d; }

    public String getRecipeInstructions()           { return recipeInstructions; }
    public void setRecipeInstructions(String i)     { this.recipeInstructions = i; }

    public int getServings()                        { return servings; }
    public void setServings(int servings)           { this.servings = servings; }

    public int getPrepTimeMinutes()                 { return prepTimeMinutes; }
    public void setPrepTimeMinutes(int v)           { this.prepTimeMinutes = v; }

    public int getCookTimeMinutes()                 { return cookTimeMinutes; }
    public void setCookTimeMinutes(int v)           { this.cookTimeMinutes = v; }

    public int getTotalTimeMinutes()                { return totalTimeMinutes; }
    public void setTotalTimeMinutes(int v)          { this.totalTimeMinutes = v; }

    public String getRecipeImage()                  { return recipeImage; }
    public void setRecipeImage(String recipeImage)  { this.recipeImage = recipeImage; }

    public List<IngredientLine> getIngredients()           { return ingredients; }
    public void setIngredients(List<IngredientLine> list)  { this.ingredients = list; }

    // ─── Helpers ─────────────────────────────────────────────────────────

    /**
     * Serialises the structured ingredient list to the compact CSV string
     * used by the Room cache ("Făină: 500.0 g, Ouă: 3.0 buc., ...").
     * Call this before inserting into CachedRecipe.
     */
    public String ingredientsToString() {
        if (ingredients == null || ingredients.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ingredients.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(ingredients.get(i).toString());
        }
        return sb.toString();
    }

    /**
     * Rebuilds the structured list from the flat cache string produced by
     * {@link #ingredientsToString()}.  Used when converting a CachedRecipe
     * back into a RecipeDetails object.
     */
    public static List<IngredientLine> ingredientsFromString(String csv) {
        List<IngredientLine> list = new ArrayList<>();
        if (csv == null || csv.isBlank()) return list;

        for (String entry : csv.split(", ")) {
            String[] parts = entry.split(": ", 2);
            if (parts.length < 2) continue;

            String ingredientName = parts[0].trim();
            String rest = parts[1].trim();          // "500.0 g" or "3.0"

            String[] amountUnit = rest.split(" ", 2);
            double qty = 0;
            try { qty = Double.parseDouble(amountUnit[0]); } catch (NumberFormatException ignored) {}
            String unit = amountUnit.length > 1 ? amountUnit[1].trim() : "";

            list.add(new IngredientLine(ingredientName, qty, unit));
        }
        return list;
    }
}