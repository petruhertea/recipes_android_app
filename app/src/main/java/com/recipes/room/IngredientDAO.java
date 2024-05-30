package com.recipes.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.recipes.models.AvailableIngredient;

import java.util.List;

@Dao
public interface IngredientDAO {

    @Insert
    public void insertIngredient(AvailableIngredient availableIngredient);

    @Update
    public void updateIngredient(AvailableIngredient availableIngredient);

    @Delete
    public void deleteIngredient(AvailableIngredient availableIngredient);

    @Query("DELETE FROM available_ingredients")
    public void deleteAll();

    @Query("SELECT * FROM available_ingredients")
    LiveData<List<AvailableIngredient>> getAllIngredients();

}
