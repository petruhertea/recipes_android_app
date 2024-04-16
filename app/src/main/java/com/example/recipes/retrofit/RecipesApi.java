package com.example.recipes.retrofit;

import com.example.recipes.models.BeverageDetails;
import com.example.recipes.models.RecipeDetails;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface RecipesApi {
    @GET("/api/recipes")
    Call<List<RecipeDetails>> getAllRecipes();

    @GET("/api/recipe/suggestions")
    Call<List<BeverageDetails>> getAllSuggestions(@Query("recipe_id") Integer recipe_id);

    @GET("/api/recipe")
    Call<RecipeDetails> getRecipeByID(@Query("recipe_id") Integer recipe_id);

    @GET("/api/recipes/byIngredients")
    Call<List<RecipeDetails>> getRecipesByIngredients(@Query("ingredients") String[] ingredients);
}
