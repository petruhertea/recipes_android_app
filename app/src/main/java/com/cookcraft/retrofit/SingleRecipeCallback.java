package com.cookcraft.retrofit;

import com.cookcraft.models.RecipeDetails;

public interface SingleRecipeCallback {

    void onRecipeDetailsReceived(RecipeDetails recipeDetails);

    void onFailure(String errorMessage);

}
