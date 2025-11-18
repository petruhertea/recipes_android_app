package com.cookcraft.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cookcraft.model.CachedRecipe;

import java.util.List;

@Dao
public interface RecipeDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecipes(List<CachedRecipe> recipes);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecipe(CachedRecipe recipe);

    @Query("SELECT * FROM cached_recipes ORDER BY recipe_title ASC")
    LiveData<List<CachedRecipe>> getAllCachedRecipes();

    @Query("SELECT * FROM cached_recipes WHERE recipeID = :id")
    CachedRecipe getRecipeById(int id);

    @Query("SELECT * FROM cached_recipes WHERE recipeID = :id")
    LiveData<CachedRecipe> getRecipeByIdLive(int id);

    @Query("DELETE FROM cached_recipes WHERE cached_timestamp < :expiryTime")
    void deleteExpiredRecipes(long expiryTime);

    @Query("DELETE FROM cached_recipes")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM cached_recipes")
    int getRecipeCount();
}