package com.cookcraft.mvvm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.cookcraft.model.AvailableIngredient;
import com.cookcraft.model.BeverageDetails;
import com.cookcraft.model.RecipeDetails;
import com.cookcraft.util.NetworkStatusHelper;

import java.util.List;

public class RecipeViewModel extends AndroidViewModel {
    private RecipeRepository repository;
    private NetworkStatusHelper networkStatusHelper;

    public RecipeViewModel(@NonNull Application application) {
        super(application);
        repository = new RecipeRepository(application);
        networkStatusHelper = new NetworkStatusHelper(application);
    }

    public LiveData<List<RecipeDetails>> getAllRecipes() {
        return repository.getAllRecipes();
    }

    public LiveData<List<RecipeDetails>> getRecipesByIngredients(List<AvailableIngredient> ingredients) {
        return repository.getRecipesByIngredients(ingredients);
    }

    public LiveData<RecipeDetails> getRecipeById(int recipeId) {
        return repository.getRecipeById(recipeId);
    }

    public LiveData<List<BeverageDetails>> getBeveragesByRecipeId(int recipeId) {
        return repository.getBeveragesByRecipeId(recipeId);
    }

    public LiveData<List<RecipeDetails>> forceRefreshAllRecipes() {
        return repository.forceRefreshAllRecipes();
    }

    public LiveData<List<RecipeDetails>> forceRefreshRecipesByIngredients(List<AvailableIngredient> ingredients) {
        return repository.forceRefreshRecipesByIngredients(ingredients);
    }

    public void cleanExpiredCache() {
        repository.cleanExpiredCache();
    }

    public LiveData<Boolean> getNetworkStatus() {
        return networkStatusHelper;
    }
}