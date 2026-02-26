package com.cookcraft.mvvm;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

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

import java.text.Normalizer;
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
    private static final long CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000L; // 24 h

    private final RecipeDAO recipeDAO;
    private final BeverageDAO beverageDAO;
    private final RecipesApi apiService;
    private final Context context;
    private final ExecutorService executor;

    public RecipeRepository(Context context) {
        this.context = context.getApplicationContext();
        CookCraftDatabase db = CookCraftDatabase.getInstance(this.context);
        this.recipeDAO  = db.recipeDAO();
        this.beverageDAO = db.beverageDAO();
        this.apiService  = RetrofitClient.getClient(this.context).create(RecipesApi.class);
        this.executor    = Executors.newSingleThreadExecutor();
    }

    // ─── All recipes (offline-first) ────────────────────────────────────

    public LiveData<List<RecipeDetails>> getAllRecipes() {
        MediatorLiveData<List<RecipeDetails>> result = buildCacheSource();

        if (NetworkCheck.hasNetwork(context)) {
            apiService.getAllRecipes().enqueue(new Callback<List<RecipeDetails>>() {
                @Override public void onResponse(Call<List<RecipeDetails>> call,
                                                 Response<List<RecipeDetails>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        result.postValue(response.body());
                        executor.execute(() -> cacheRecipes(response.body()));
                    }
                }
                @Override public void onFailure(Call<List<RecipeDetails>> call, Throwable t) {
                    Log.e(TAG, "getAllRecipes network failure", t);
                }
            });
        }
        return result;
    }

    // ─── Filtered by ingredients ─────────────────────────────────────────

    public LiveData<List<RecipeDetails>> getRecipesByIngredients(List<AvailableIngredient> ingredients) {
        MediatorLiveData<List<RecipeDetails>> result = new MediatorLiveData<>();

        if (NetworkCheck.hasNetwork(context)) {
            apiService.postRecipesByIngredients(toIngredientMap(ingredients))
                    .enqueue(new Callback<List<RecipeDetails>>() {
                        @Override public void onResponse(Call<List<RecipeDetails>> call,
                                                         Response<List<RecipeDetails>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                result.postValue(response.body());
                                executor.execute(() -> cacheRecipes(response.body()));
                            }
                        }
                        @Override public void onFailure(Call<List<RecipeDetails>> call, Throwable t) {
                            Log.e(TAG, "getRecipesByIngredients network failure", t);
                            attachCacheSource(result);
                        }
                    });
        } else {
            attachCacheSource(result);
        }
        return result;
    }

    // ─── Single recipe by ID ─────────────────────────────────────────────

    public LiveData<RecipeDetails> getRecipeById(int recipeId) {
        MediatorLiveData<RecipeDetails> result = new MediatorLiveData<>();

        LiveData<CachedRecipe> cached = recipeDAO.getRecipeByIdLive(recipeId);
        result.addSource(cached, cr -> {
            if (cr != null) result.setValue(cachedToDetail(cr));
        });

        if (NetworkCheck.hasNetwork(context)) {
            apiService.getRecipeByID(recipeId).enqueue(new Callback<RecipeDetails>() {
                @Override public void onResponse(Call<RecipeDetails> call,
                                                 Response<RecipeDetails> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        result.postValue(response.body());
                        executor.execute(() -> cacheRecipe(response.body()));
                    }
                }
                @Override public void onFailure(Call<RecipeDetails> call, Throwable t) {
                    Log.e(TAG, "getRecipeById network failure", t);
                }
            });
        }
        return result;
    }

    // ─── Beverages for a recipe ───────────────────────────────────────────

    public LiveData<List<BeverageDetails>> getBeveragesByRecipeId(int recipeId) {
        MediatorLiveData<List<BeverageDetails>> result = new MediatorLiveData<>();

        LiveData<List<CachedBeverage>> cached = beverageDAO.getBeveragesByRecipeId(recipeId);
        result.addSource(cached, list -> {
            if (list != null && !list.isEmpty())
                result.setValue(cachedToBeverages(list));
        });

        if (NetworkCheck.hasNetwork(context)) {
            apiService.getAllSuggestions(recipeId).enqueue(new Callback<List<BeverageDetails>>() {
                @Override public void onResponse(Call<List<BeverageDetails>> call,
                                                 Response<List<BeverageDetails>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        result.postValue(response.body());
                        executor.execute(() -> cacheBeverages(recipeId, response.body()));
                    }
                }
                @Override public void onFailure(Call<List<BeverageDetails>> call, Throwable t) {
                    Log.e(TAG, "getBeverages network failure", t);
                }
            });
        }
        return result;
    }

    // ─── Force refresh ───────────────────────────────────────────────────

    public LiveData<List<RecipeDetails>> forceRefreshAllRecipes() {
        MediatorLiveData<List<RecipeDetails>> result = new MediatorLiveData<>();
        if (!NetworkCheck.hasNetwork(context)) {
            attachCacheSource(result);
            return result;
        }
        apiService.getAllRecipes().enqueue(new Callback<List<RecipeDetails>>() {
            @Override public void onResponse(Call<List<RecipeDetails>> call,
                                             Response<List<RecipeDetails>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(response.body());
                    executor.execute(() -> cacheRecipes(response.body()));
                } else {
                    attachCacheSource(result);
                }
            }
            @Override public void onFailure(Call<List<RecipeDetails>> call, Throwable t) {
                Log.e(TAG, "forceRefreshAllRecipes failure", t);
                attachCacheSource(result);
            }
        });
        return result;
    }

    public LiveData<List<RecipeDetails>> forceRefreshRecipesByIngredients(
            List<AvailableIngredient> ingredients) {
        MediatorLiveData<List<RecipeDetails>> result = new MediatorLiveData<>();
        if (!NetworkCheck.hasNetwork(context)) {
            attachFilteredCacheSource(result, ingredients);
            return result;
        }
        apiService.postRecipesByIngredients(toIngredientMap(ingredients))
                .enqueue(new Callback<List<RecipeDetails>>() {
                    @Override public void onResponse(Call<List<RecipeDetails>> call,
                                                     Response<List<RecipeDetails>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.postValue(response.body());
                            executor.execute(() -> cacheRecipes(response.body()));
                        } else {
                            attachFilteredCacheSource(result, ingredients);
                        }
                    }
                    @Override public void onFailure(Call<List<RecipeDetails>> call, Throwable t) {
                        Log.e(TAG, "forceRefresh byIngredients failure", t);
                        attachFilteredCacheSource(result, ingredients);
                    }
                });
        return result;
    }

    // ─── Cache management ────────────────────────────────────────────────

    public void cleanExpiredCache() {
        executor.execute(() -> {
            long cutoff = System.currentTimeMillis() - CACHE_EXPIRY_MS;
            recipeDAO.deleteExpiredRecipes(cutoff);
            beverageDAO.deleteExpiredBeverages(cutoff);
            Log.d(TAG, "Expired cache cleaned");
        });
    }

    // ─── Private helpers ─────────────────────────────────────────────────

    /** Wraps the Room LiveData source into a MediatorLiveData. */
    @NonNull
    private MediatorLiveData<List<RecipeDetails>> buildCacheSource() {
        MediatorLiveData<List<RecipeDetails>> result = new MediatorLiveData<>();
        result.addSource(recipeDAO.getAllCachedRecipes(), cached -> {
            if (cached != null && !cached.isEmpty())
                result.setValue(cachedToDetails(cached));
        });
        return result;
    }

    private void attachCacheSource(MediatorLiveData<List<RecipeDetails>> result) {
        result.addSource(recipeDAO.getAllCachedRecipes(), cached -> {
            if (cached != null)
                result.setValue(cachedToDetails(cached));
        });
    }

    private void attachFilteredCacheSource(MediatorLiveData<List<RecipeDetails>> result,
                                           List<AvailableIngredient> ingredients) {
        result.addSource(recipeDAO.getAllCachedRecipes(), cached -> {
            if (cached != null) {
                List<RecipeDetails> all = cachedToDetails(cached);
                result.setValue(filterLocally(all, ingredients));
            }
        });
    }

    private void cacheRecipes(List<RecipeDetails> recipes) {
        long ts = System.currentTimeMillis();
        List<CachedRecipe> list = new ArrayList<>();
        for (RecipeDetails r : recipes) list.add(detailToCached(r, ts));
        recipeDAO.insertRecipes(list);
        Log.d(TAG, "Cached " + list.size() + " recipes");
    }

    private void cacheRecipe(RecipeDetails recipe) {
        recipeDAO.insertRecipe(detailToCached(recipe, System.currentTimeMillis()));
    }

    private void cacheBeverages(int recipeId, List<BeverageDetails> beverages) {
        beverageDAO.deleteBeveragesByRecipeId(recipeId);
        long ts = System.currentTimeMillis();
        List<CachedBeverage> list = new ArrayList<>();
        for (BeverageDetails b : beverages) {
            list.add(new CachedBeverage(b.getId(), recipeId, b.getName(),
                    b.getBeverageImage(), ts));
        }
        beverageDAO.insertBeverages(list);
        Log.d(TAG, "Cached " + list.size() + " beverages for recipe " + recipeId);
    }

    // ─── Conversion ──────────────────────────────────────────────────────

    private CachedRecipe detailToCached(RecipeDetails r, long timestamp) {
        return new CachedRecipe(
                r.getRecipeID(), r.getRecipeTitle(), r.getRecipeDescription(),
                r.getRecipeInstructions(), r.getServings(), r.getPrepTimeMinutes(),
                r.getCookTimeMinutes(), r.getTotalTimeMinutes(), r.getRecipeImage(),
                r.ingredientsToString(),   // serialize List → CSV for Room
                timestamp
        );
    }

    private RecipeDetails cachedToDetail(CachedRecipe c) {
        RecipeDetails d = new RecipeDetails();
        d.setRecipeID(c.getRecipeID());
        d.setRecipeTitle(c.getRecipeTitle());
        d.setRecipeDescription(c.getRecipeDescription());
        d.setRecipeInstructions(c.getRecipeInstructions());
        d.setServings(c.getServings());
        d.setPrepTimeMinutes(c.getPrepTimeMinutes());
        d.setCookTimeMinutes(c.getCookTimeMinutes());
        d.setTotalTimeMinutes(c.getTotalTimeMinutes());
        d.setRecipeImage(c.getRecipeImage());
        // deserialize CSV → List<IngredientLine>
        d.setIngredients(RecipeDetails.ingredientsFromString(c.getIngredients()));
        return d;
    }

    private List<RecipeDetails> cachedToDetails(List<CachedRecipe> cached) {
        List<RecipeDetails> list = new ArrayList<>();
        for (CachedRecipe c : cached) list.add(cachedToDetail(c));
        return list;
    }

    private List<BeverageDetails> cachedToBeverages(List<CachedBeverage> cached) {
        List<BeverageDetails> list = new ArrayList<>();
        for (CachedBeverage c : cached) {
            BeverageDetails b = new BeverageDetails();
            b.setId(c.getBeverageId());
            b.setName(c.getName());
            b.setBeverageImage(c.getBeverageImage());
            list.add(b);
        }
        return list;
    }

    // ─── Local ingredient filtering (offline fallback) ───────────────────

    private List<RecipeDetails> filterLocally(List<RecipeDetails> all,
                                              List<AvailableIngredient> userIngredients) {
        if (userIngredients == null || userIngredients.isEmpty()) return all;

        Map<String, AvailableIngredient> userMap = new HashMap<>();
        for (AvailableIngredient i : userIngredients)
            userMap.put(normalize(i.getName()), i);

        List<RecipeDetails> matched = new ArrayList<>();
        for (RecipeDetails recipe : all) {
            List<RecipeDetails.IngredientLine> lines = recipe.getIngredients();
            if (lines == null || lines.isEmpty()) continue;

            int hits = 0;
            for (RecipeDetails.IngredientLine line : lines) {
                if (userMap.containsKey(normalize(line.getName()))) hits++;
            }
            if (hits >= lines.size() * 0.5) matched.add(recipe);
        }
        Log.d(TAG, "Local filter: " + matched.size() + "/" + all.size() + " recipes matched");
        return matched;
    }

    private Map<String, IngredientDetails> toIngredientMap(List<AvailableIngredient> ingredients) {
        Map<String, IngredientDetails> map = new HashMap<>();
        for (AvailableIngredient i : ingredients)
            map.put(i.getName(), new IngredientDetails(i.getQuantity(), i.getMeasureUnit()));
        return map;
    }

    private String normalize(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase().trim();
    }
}