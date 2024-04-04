package com.example.recipes.retrofit;

import com.example.recipes.models.RecipeDetails;

import java.util.List;

public interface SingleRecipeCallback {

    void onRecipeDetailsReceived(RecipeDetails recipeDetails);
    void onFailure(String errorMessage);

}
