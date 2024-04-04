package com.example.recipes.retrofit;

import com.example.recipes.models.RecipeDetails;

import java.util.List;

public interface RecipeDetailsCallback {
    void onRecipeDetailsReceived(List<RecipeDetails> recipeDetails);
    void onFailure(String errorMessage);
}
