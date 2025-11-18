package com.cookcraft.mvvm;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.cookcraft.model.AvailableIngredient;
import com.cookcraft.model.BeverageDetails;
import com.cookcraft.model.CachedBeverage;
import com.cookcraft.model.CachedRecipe;
import com.cookcraft.model.IngredientDetails;
import com.cookcraft.model.RecipeDetails;
import com.cookcraft.retrofit.NetworkCheck;
import com.cookcraft.retrofit.RecipesApi;
import com.cookcraft.retrofit.RetrofitClient;
import com.cookcraft.room.BeverageDAO;
import com.cookcraft.room.CookCraftDatabase;
import com.cookcraft.room.RecipeDAO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeRepository {
    private static final String TAG = "RecipeRepository";
    private static final long CACHE_EXPIRY_TIME = 24 * 60 * 60 * 1000; // 24 hours

    private final RecipeDAO recipeDAO;
    private final BeverageDAO beverageDAO;
    private final RecipesApi apiService;
    private final Context context;
    private final ExecutorService executorService;

    public RecipeRepository(Context context) {
        this.context = context;
        CookCraftDatabase database = CookCraftDatabase.getInstance(context);
        this.recipeDAO = database.recipeDAO();
        this.beverageDAO = database.beverageDAO();
        this.apiService = RetrofitClient.getClient(context).create(RecipesApi.class);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // Get all recipes (offline-first) - CORRECTED VERSION
    public LiveData<List<RecipeDetails>> getAllRecipes() {
        MediatorLiveData<List<RecipeDetails>> result = getListMediatorLiveData();

        // Fetch fresh data from network if online
        if (NetworkCheck.hasNetwork(context)) {
            apiService.getAllRecipes().enqueue(new Callback<List<RecipeDetails>>() {
                @Override
                public void onResponse(Call<List<RecipeDetails>> call, Response<List<RecipeDetails>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Update the result immediately with fresh data
                        result.postValue(response.body());

                        // Cache in background
                        executorService.execute(() -> cacheRecipes(response.body()));
                    }
                }

                @Override
                public void onFailure(Call<List<RecipeDetails>> call, Throwable t) {
                    Log.e(TAG, "Failed to refresh recipes from network", t);
                    // Keep showing cached data - no action needed
                }
            });
        }

        return result;
    }

    @NonNull
    private MediatorLiveData<List<RecipeDetails>> getListMediatorLiveData() {
        MediatorLiveData<List<RecipeDetails>> result = new MediatorLiveData<>();

        // Get cached data as LiveData
        LiveData<List<CachedRecipe>> cachedSource = recipeDAO.getAllCachedRecipes();

        // Add cached data as source - automatically updates UI when cache changes
        result.addSource(cachedSource, cachedRecipes -> {
            if (cachedRecipes != null && !cachedRecipes.isEmpty()) {
                result.setValue(convertCachedToDetails(cachedRecipes));
            }
        });
        return result;
    }

    // Get recipes by ingredients (offline-first)
    public LiveData<List<RecipeDetails>> getRecipesByIngredients(List<AvailableIngredient> ingredients) {
        MediatorLiveData<List<RecipeDetails>> result = new MediatorLiveData<>();

        if (NetworkCheck.hasNetwork(context)) {
            // Online: fetch from API
            Map<String, IngredientDetails> ingredientMap = new HashMap<>();
            for (AvailableIngredient ingredient : ingredients) {
                ingredientMap.put(ingredient.getName(),
                        new IngredientDetails(ingredient.getQuantity(), ingredient.getMeasureUnit()));
            }

            apiService.postRecipesByIngredients(ingredientMap).enqueue(new Callback<List<RecipeDetails>>() {
                @Override
                public void onResponse(Call<List<RecipeDetails>> call, Response<List<RecipeDetails>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        result.postValue(response.body());

                        // Cache the results
                        executorService.execute(() -> cacheRecipes(response.body()));
                    }
                }

                @Override
                public void onFailure(Call<List<RecipeDetails>> call, Throwable t) {
                    Log.e(TAG, "Failed to fetch recipes by ingredients", t);
                    // Fallback to all cached recipes
                    LiveData<List<CachedRecipe>> cachedSource = recipeDAO.getAllCachedRecipes();
                    result.addSource(cachedSource, cachedRecipes -> {
                        if (cachedRecipes != null && !cachedRecipes.isEmpty()) {
                            result.setValue(convertCachedToDetails(cachedRecipes));
                        }
                    });
                }
            });
        } else {
            // Offline: use all cached data
            LiveData<List<CachedRecipe>> cachedSource = recipeDAO.getAllCachedRecipes();
            result.addSource(cachedSource, cachedRecipes -> {
                if (cachedRecipes != null && !cachedRecipes.isEmpty()) {
                    result.setValue(convertCachedToDetails(cachedRecipes));
                }
            });
        }

        return result;
    }

    // Get single recipe by ID (offline-first)
    public LiveData<RecipeDetails> getRecipeById(int recipeId) {
        MediatorLiveData<RecipeDetails> result = new MediatorLiveData<>();

        // Get cached recipe as LiveData
        LiveData<CachedRecipe> cachedSource = recipeDAO.getRecipeByIdLive(recipeId);

        // Add cached data as source
        result.addSource(cachedSource, cachedRecipe -> {
            if (cachedRecipe != null) {
                result.setValue(convertCachedToDetail(cachedRecipe));
            }
        });

        // Fetch fresh data if online
        if (NetworkCheck.hasNetwork(context)) {
            apiService.getRecipeByID(recipeId).enqueue(new Callback<RecipeDetails>() {
                @Override
                public void onResponse(Call<RecipeDetails> call, Response<RecipeDetails> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        result.postValue(response.body());

                        // Cache it
                        executorService.execute(() -> cacheRecipe(response.body()));
                    }
                }

                @Override
                public void onFailure(Call<RecipeDetails> call, Throwable t) {
                    Log.e(TAG, "Failed to fetch recipe by ID", t);
                }
            });
        }

        return result;
    }

    // Get beverages for a recipe (offline-first)
    public LiveData<List<BeverageDetails>> getBeveragesByRecipeId(int recipeId) {
        MediatorLiveData<List<BeverageDetails>> result = new MediatorLiveData<>();

        // Get cached beverages as LiveData
        LiveData<List<CachedBeverage>> cachedSource = beverageDAO.getBeveragesByRecipeId(recipeId);

        // Add cached data as source
        result.addSource(cachedSource, cachedBeverages -> {
            if (cachedBeverages != null && !cachedBeverages.isEmpty()) {
                result.setValue(convertCachedBeveragesToDetails(cachedBeverages));
            }
        });

        // Fetch fresh data if online
        if (NetworkCheck.hasNetwork(context)) {
            apiService.getAllSuggestions(recipeId).enqueue(new Callback<List<BeverageDetails>>() {
                @Override
                public void onResponse(Call<List<BeverageDetails>> call, Response<List<BeverageDetails>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        result.postValue(response.body());

                        // Cache it
                        executorService.execute(() -> cacheBeverages(recipeId, response.body()));
                    }
                }

                @Override
                public void onFailure(Call<List<BeverageDetails>> call, Throwable t) {
                    Log.e(TAG, "Failed to fetch beverages", t);
                }
            });
        }

        return result;
    }

    // Helper: Cache recipes
    private void cacheRecipes(List<RecipeDetails> recipes) {
        long timestamp = System.currentTimeMillis();
        List<CachedRecipe> cachedRecipes = new ArrayList<>();

        for (RecipeDetails recipe : recipes) {
            cachedRecipes.add(new CachedRecipe(
                    recipe.getRecipeID(),
                    recipe.getRecipeTitle(),
                    recipe.getRecipeDescription(),
                    recipe.getRecipeInstructions(),
                    recipe.getServings(),
                    recipe.getPrepTimeMinutes(),
                    recipe.getCookTimeMinutes(),
                    recipe.getTotalTimeMinutes(),
                    recipe.getRecipeImage(),
                    recipe.getIngredients(),
                    timestamp
            ));
        }

        recipeDAO.insertRecipes(cachedRecipes);
        Log.d(TAG, "Cached " + cachedRecipes.size() + " recipes");
    }

    // Helper: Cache single recipe
    private void cacheRecipe(RecipeDetails recipe) {
        long timestamp = System.currentTimeMillis();
        CachedRecipe cached = new CachedRecipe(
                recipe.getRecipeID(),
                recipe.getRecipeTitle(),
                recipe.getRecipeDescription(),
                recipe.getRecipeInstructions(),
                recipe.getServings(),
                recipe.getPrepTimeMinutes(),
                recipe.getCookTimeMinutes(),
                recipe.getTotalTimeMinutes(),
                recipe.getRecipeImage(),
                recipe.getIngredients(),
                timestamp
        );

        recipeDAO.insertRecipe(cached);
        Log.d(TAG, "Cached recipe: " + recipe.getRecipeTitle());
    }

    // Helper: Cache beverages
    private void cacheBeverages(int recipeId, List<BeverageDetails> beverages) {
        long timestamp = System.currentTimeMillis();
        List<CachedBeverage> cachedBeverages = new ArrayList<>();

        // First delete old beverages for this recipe
        beverageDAO.deleteBeveragesByRecipeId(recipeId);

        for (BeverageDetails beverage : beverages) {
            cachedBeverages.add(new CachedBeverage(
                    recipeId,
                    beverage.getBeverageSuggestions(),
                    beverage.getBeverageImage(),
                    timestamp
            ));
        }

        beverageDAO.insertBeverages(cachedBeverages);
        Log.d(TAG, "Cached " + cachedBeverages.size() + " beverages for recipe " + recipeId);
    }

    // Conversion helpers
    private List<RecipeDetails> convertCachedToDetails(List<CachedRecipe> cached) {
        List<RecipeDetails> details = new ArrayList<>();
        for (CachedRecipe recipe : cached) {
            details.add(convertCachedToDetail(recipe));
        }
        return details;
    }

    private RecipeDetails convertCachedToDetail(CachedRecipe cached) {
        RecipeDetails details = new RecipeDetails();
        details.setRecipeID(cached.getRecipeID());
        details.setRecipeTitle(cached.getRecipeTitle());
        details.setRecipeDescription(cached.getRecipeDescription());
        details.setRecipeInstructions(cached.getRecipeInstructions());
        details.setServings(cached.getServings());
        details.setPrepTimeMinutes(cached.getPrepTimeMinutes());
        details.setCookTimeMinutes(cached.getCookTimeMinutes());
        details.setTotalTimeMinutes(cached.getTotalTimeMinutes());
        details.setRecipeImage(cached.getRecipeImage());
        details.setIngredients(cached.getIngredients());
        return details;
    }

    private List<BeverageDetails> convertCachedBeveragesToDetails(List<CachedBeverage> cached) {
        List<BeverageDetails> details = new ArrayList<>();
        for (CachedBeverage beverage : cached) {
            BeverageDetails detail = new BeverageDetails();
            detail.setRecipeID(beverage.getRecipeID());
            detail.setBeverageSuggestions(beverage.getBeverageSuggestions());
            detail.setBeverageImage(beverage.getBeverageImage());
            details.add(detail);
        }
        return details;
    }

    // Helper: Filter recipes locally by ingredients (for offline use)
    private List<RecipeDetails> filterRecipesLocally(List<RecipeDetails> allRecipes,
                                                     List<AvailableIngredient> userIngredients) {
        if (userIngredients == null || userIngredients.isEmpty()) {
            return allRecipes;
        }

        List<RecipeDetails> matchedRecipes = new ArrayList<>();

        // Create map of user's ingredients (normalized)
        Map<String, AvailableIngredient> userIngredientMap = new HashMap<>();
        for (AvailableIngredient ingredient : userIngredients) {
            String normalizedName = normalizeString(ingredient.getName());
            userIngredientMap.put(normalizedName, ingredient);
        }

        // Filter recipes
        for (RecipeDetails recipe : allRecipes) {
            String ingredientsStr = recipe.getIngredients();
            if (ingredientsStr == null || ingredientsStr.isEmpty()) {
                continue;
            }

            String[] recipeIngredients = ingredientsStr.split(", ");
            int matchedCount = 0;
            int totalRequired = recipeIngredients.length;

            for (String recipeIngredient : recipeIngredients) {
                String[] parts = recipeIngredient.split(": ");
                if (parts.length < 2) continue;

                String ingredientName = normalizeString(parts[0].trim());

                // Check if user has this ingredient
                if (userIngredientMap.containsKey(ingredientName)) {
                    matchedCount++;
                }
            }

            // If recipe matches at least 50% of ingredients, include it
            if (matchedCount >= totalRequired * 0.5) {
                matchedRecipes.add(recipe);
            }
        }

        Log.d(TAG, "Local filtering: " + matchedRecipes.size() + " recipes matched from " + allRecipes.size());
        return matchedRecipes;
    }

    // Helper: Normalize string for comparison
    private String normalizeString(String str) {
        // Remove accents and convert to lowercase
        String normalized = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase().trim();
    }

    // Force refresh from network (for pull-to-refresh)
    public LiveData<List<RecipeDetails>> forceRefreshAllRecipes() {
        MediatorLiveData<List<RecipeDetails>> result = new MediatorLiveData<>();

        if (!NetworkCheck.hasNetwork(context)) {
            // If offline, just return cached data
            LiveData<List<CachedRecipe>> cachedSource = recipeDAO.getAllCachedRecipes();
            result.addSource(cachedSource, cachedRecipes -> {
                if (cachedRecipes != null) {
                    result.setValue(convertCachedToDetails(cachedRecipes));
                }
            });
            return result;
        }

        // Force fetch from network
        apiService.getAllRecipes().enqueue(new Callback<List<RecipeDetails>>() {
            @Override
            public void onResponse(Call<List<RecipeDetails>> call, Response<List<RecipeDetails>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(response.body());

                    // Update cache
                    executorService.execute(() -> cacheRecipes(response.body()));
                } else {
                    // On failure, return cached data
                    LiveData<List<CachedRecipe>> cachedSource = recipeDAO.getAllCachedRecipes();
                    result.addSource(cachedSource, cachedRecipes -> {
                        if (cachedRecipes != null) {
                            result.setValue(convertCachedToDetails(cachedRecipes));
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<RecipeDetails>> call, Throwable t) {
                Log.e(TAG, "Force refresh failed", t);
                // Return cached data on failure
                LiveData<List<CachedRecipe>> cachedSource = recipeDAO.getAllCachedRecipes();
                result.addSource(cachedSource, cachedRecipes -> {
                    if (cachedRecipes != null) {
                        result.setValue(convertCachedToDetails(cachedRecipes));
                    }
                });
            }
        });

        return result;
    }

    // Force refresh recipes by ingredients - WITH LOCAL FILTERING
    public LiveData<List<RecipeDetails>> forceRefreshRecipesByIngredients(List<AvailableIngredient> ingredients) {
        MediatorLiveData<List<RecipeDetails>> result = new MediatorLiveData<>();

        if (!NetworkCheck.hasNetwork(context)) {
            // If offline, return locally filtered cached data
            Log.d(TAG, "Offline: filtering cached recipes locally");
            LiveData<List<CachedRecipe>> cachedSource = recipeDAO.getAllCachedRecipes();
            result.addSource(cachedSource, cachedRecipes -> {
                if (cachedRecipes != null) {
                    List<RecipeDetails> allRecipes = convertCachedToDetails(cachedRecipes);
                    List<RecipeDetails> filteredRecipes = filterRecipesLocally(allRecipes, ingredients);
                    result.setValue(filteredRecipes);
                }
            });
            return result;
        }

        // Force fetch from network with ingredient filtering
        Map<String, IngredientDetails> ingredientMap = new HashMap<>();
        for (AvailableIngredient ingredient : ingredients) {
            ingredientMap.put(ingredient.getName(),
                    new IngredientDetails(ingredient.getQuantity(), ingredient.getMeasureUnit()));
        }

        Log.d(TAG, "Force refreshing recipes with " + ingredients.size() + " ingredients");

        apiService.postRecipesByIngredients(ingredientMap).enqueue(new Callback<List<RecipeDetails>>() {
            @Override
            public void onResponse(Call<List<RecipeDetails>> call, Response<List<RecipeDetails>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully fetched " + response.body().size() + " filtered recipes");
                    result.postValue(response.body());

                    // Update cache
                    executorService.execute(() -> cacheRecipes(response.body()));
                } else {
                    Log.e(TAG, "Response unsuccessful, filtering cached recipes locally");
                    // On failure, filter cached data locally
                    LiveData<List<CachedRecipe>> cachedSource = recipeDAO.getAllCachedRecipes();
                    result.addSource(cachedSource, cachedRecipes -> {
                        if (cachedRecipes != null) {
                            List<RecipeDetails> allRecipes = convertCachedToDetails(cachedRecipes);
                            List<RecipeDetails> filteredRecipes = filterRecipesLocally(allRecipes, ingredients);
                            result.setValue(filteredRecipes);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<RecipeDetails>> call, Throwable t) {
                Log.e(TAG, "Force refresh by ingredients failed: " + t.getMessage());
                // Filter cached data locally
                LiveData<List<CachedRecipe>> cachedSource = recipeDAO.getAllCachedRecipes();
                result.addSource(cachedSource, cachedRecipes -> {
                    if (cachedRecipes != null) {
                        List<RecipeDetails> allRecipes = convertCachedToDetails(cachedRecipes);
                        List<RecipeDetails> filteredRecipes = filterRecipesLocally(allRecipes, ingredients);
                        result.setValue(filteredRecipes);
                    }
                });
            }
        });

        return result;
    }

    // Clean up expired cache
    public void cleanExpiredCache() {
        executorService.execute(() -> {
            long expiryTime = System.currentTimeMillis() - CACHE_EXPIRY_TIME;
            recipeDAO.deleteExpiredRecipes(expiryTime);
            beverageDAO.deleteExpiredBeverages(expiryTime);
            Log.d(TAG, "Cleaned expired cache");
        });
    }
}