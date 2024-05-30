package com.recipes.retrofit;

import com.recipes.models.RecipeDetails;

import java.util.List;

public interface RecipeDetailsCallback {
    void onRecipeDetailsReceived(List<RecipeDetails> recipeDetails);

    void onFailure(String errorMessage);
}
