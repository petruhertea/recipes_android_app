package com.cookcraft.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity for the offline beverage cache.
 *
 * The old "beverage_suggestions" column (which stored the beverage name)
 * has been renamed to "name" to match the updated BeverageDetails model.
 * The "beverage_id" column is added so we can reconstruct BeverageDetails.id.
 *
 * ⚠️  Schema version bump required: increment the Room database version
 *     and provide a migration (or keep fallbackToDestructiveMigration for dev).
 */
@Entity(tableName = "cached_beverages")
public class CachedBeverage {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "beverage_id")
    private int beverageId;

    @ColumnInfo(name = "recipe_id")
    private int recipeID;

    /** The display name of the beverage (was "beverage_suggestions"). */
    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "beverage_image")
    private String beverageImage;

    @ColumnInfo(name = "cached_timestamp")
    private long cachedTimestamp;

    public CachedBeverage(int beverageId, int recipeID, String name,
                          String beverageImage, long cachedTimestamp) {
        this.beverageId = beverageId;
        this.recipeID = recipeID;
        this.name = name;
        this.beverageImage = beverageImage;
        this.cachedTimestamp = cachedTimestamp;
    }

    // ─── Getters & Setters ───────────────────────────────────────────────

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public int getBeverageId()                  { return beverageId; }
    public void setBeverageId(int beverageId)   { this.beverageId = beverageId; }

    public int getRecipeID()                    { return recipeID; }
    public void setRecipeID(int recipeID)       { this.recipeID = recipeID; }

    public String getName()                     { return name; }
    public void setName(String name)            { this.name = name; }

    public String getBeverageImage()            { return beverageImage; }
    public void setBeverageImage(String img)    { this.beverageImage = img; }

    public long getCachedTimestamp()            { return cachedTimestamp; }
    public void setCachedTimestamp(long ts)     { this.cachedTimestamp = ts; }
}