package com.cookcraft.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cookcraft.model.CachedBeverage;

import java.util.List;

@Dao
public interface BeverageDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBeverages(List<CachedBeverage> beverages);

    @Query("SELECT * FROM cached_beverages WHERE recipe_id = :recipeId")
    LiveData<List<CachedBeverage>> getBeveragesByRecipeId(int recipeId);

    @Query("SELECT * FROM cached_beverages WHERE recipe_id = :recipeId")
    List<CachedBeverage> getBeveragesByRecipeIdSync(int recipeId);

    @Query("DELETE FROM cached_beverages WHERE recipe_id = :recipeId")
    void deleteBeveragesByRecipeId(int recipeId);

    @Query("DELETE FROM cached_beverages WHERE cached_timestamp < :expiryTime")
    void deleteExpiredBeverages(long expiryTime);

    @Query("DELETE FROM cached_beverages")
    void deleteAll();
}
