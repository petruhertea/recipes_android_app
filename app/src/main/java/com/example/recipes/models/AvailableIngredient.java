package com.example.recipes.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "available_ingredients")
public class AvailableIngredient {

    @PrimaryKey(autoGenerate = true)
    private int ingredientID;
    private String name;
    private double quantity;

    public int getIngredientID() {
        return ingredientID;
    }

    public void setIngredientID(int ingredientID) {
        this.ingredientID = ingredientID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}
