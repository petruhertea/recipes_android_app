package com.cookcraft.room;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.cookcraft.model.AvailableIngredient;
import com.cookcraft.model.CachedBeverage;
import com.cookcraft.model.CachedRecipe;

/**
 * Room database — version history:
 *
 *  v1  Original schema.
 *  v2  CachedBeverage: renamed "beverage_suggestions" → "name",
 *                      added    "beverage_id" INT column.
 */
@Database(
        entities = {AvailableIngredient.class, CachedRecipe.class, CachedBeverage.class},
        version = 2,
        exportSchema = false
)
public abstract class CookCraftDatabase extends RoomDatabase {

    private static volatile CookCraftDatabase instance;

    // ─── Migration v1 → v2 ───────────────────────────────────────────────
    // The cached_beverages table gains a beverage_id column and renames
    // beverage_suggestions to name. Simplest safe approach: drop + recreate.
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // Drop old table
            db.execSQL("DROP TABLE IF EXISTS cached_beverages");

            // Recreate with new schema
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `cached_beverages` ("
                            + "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                            + "`beverage_id` INTEGER NOT NULL, "
                            + "`recipe_id` INTEGER NOT NULL, "
                            + "`name` TEXT, "
                            + "`beverage_image` TEXT, "
                            + "`cached_timestamp` INTEGER NOT NULL)"
            );
        }
    };

    // ─── Singleton ────────────────────────────────────────────────────────

    public static synchronized CookCraftDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            CookCraftDatabase.class,
                            "cookcraft_database")
                    .addMigrations(MIGRATION_1_2)
                    // Keep for dev convenience — remove before production release
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    // ─── DAOs ─────────────────────────────────────────────────────────────

    public abstract IngredientDAO ingredientDAO();
    public abstract RecipeDAO recipeDAO();
    public abstract BeverageDAO beverageDAO();
}