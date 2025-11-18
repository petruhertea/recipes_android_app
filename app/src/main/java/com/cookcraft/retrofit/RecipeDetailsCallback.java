package com.cookcraft.retrofit;

import com.cookcraft.model.RecipeDetails;

import java.util.List;

public interface RecipeDetailsCallback {
    void onRecipeDetailsReceived(List<RecipeDetails> recipeDetails);

    void onFailure(String errorMessage);
}
