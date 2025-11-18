package com.cookcraft.retrofit;

import com.cookcraft.model.RecipeDetails;

public interface SingleRecipeCallback {

    void onRecipeDetailsReceived(RecipeDetails recipeDetails);

    void onFailure(String errorMessage);

}
