package com.cookcraft.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.cookcraft.model.AvailableIngredient;
import com.cookcraft.model.CachedBeverage;
import com.cookcraft.model.CachedRecipe;

@Database(entities = {AvailableIngredient.class, CachedRecipe.class, CachedBeverage.class}, version = 1, exportSchema = false)
public abstract class CookCraftDatabase extends RoomDatabase {

    public static CookCraftDatabase instance;

    public static synchronized CookCraftDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            CookCraftDatabase.class,
                            "cookcraft_database").fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract IngredientDAO ingredientDAO();

    public abstract RecipeDAO recipeDAO();

    public abstract BeverageDAO beverageDAO();
}
