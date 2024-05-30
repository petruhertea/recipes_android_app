package com.recipes.retrofit;

import com.recipes.models.RecipeDetails;

public interface SingleRecipeCallback {

    void onRecipeDetailsReceived(RecipeDetails recipeDetails);

    void onFailure(String errorMessage);

}
