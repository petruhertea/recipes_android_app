package com.cookcraft.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "available_ingredients")
public class AvailableIngredient {

    @PrimaryKey(autoGenerate = true)
    private int ingredientID;
    private String name;
    private double quantity;
    @ColumnInfo(name = "measure_unit")
    private String measureUnit;


    public AvailableIngredient(String name, double quantity, String measureUnit) {
        this.name = name;
        this.quantity = quantity;
        this.measureUnit = measureUnit;
    }

    public String getMeasureUnit() {
        return measureUnit;
    }

    public void setMeasureUnit(String measureUnit) {
        this.measureUnit = measureUnit;
    }

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
