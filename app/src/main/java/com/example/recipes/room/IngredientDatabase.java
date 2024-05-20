package com.example.recipes.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.recipes.models.AvailableIngredient;

@Database(entities = AvailableIngredient.class, version = 1)
public abstract class IngredientDatabase extends RoomDatabase {

    public static IngredientDatabase instance;

    public static synchronized IngredientDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            IngredientDatabase.class,
                            "ingredient_database").fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract IngredientDAO ingredientDAO();
}
