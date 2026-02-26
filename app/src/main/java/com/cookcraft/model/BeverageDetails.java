package com.cookcraft.model;

import com.google.gson.annotations.SerializedName;

/**
 * API response model for a beverage suggestion.
 *
 * Matches the backend BeverageDTO shape:
 *   - "id"           → id
 *   - "name"         → name          (was "beverageSuggestions" / "recipeID")
 *   - "beverageImage"→ beverageImage
 */
public class BeverageDetails {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("beverageImage")
    private String beverageImage;

    public BeverageDetails() {}

    public int getId()              { return id; }
    public void setId(int id)       { this.id = id; }

    public String getName()         { return name; }
    public void setName(String name){ this.name = name; }

    public String getBeverageImage()              { return beverageImage; }
    public void setBeverageImage(String beverageImage) { this.beverageImage = beverageImage; }
}