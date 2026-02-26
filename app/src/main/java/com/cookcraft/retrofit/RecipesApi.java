package com.cookcraft.retrofit;

import com.cookcraft.model.BeverageDetails;
import com.cookcraft.model.IngredientDetails;
import com.cookcraft.model.RecipeDetails;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RecipesApi {
    @GET("/api/v1/recipes")
    Call<List<RecipeDetails>> getAllRecipes();

    @GET("/api/v1/recipes/{recipeID}/beverages")
    Call<List<BeverageDetails>> getAllSuggestions(@Path("recipeID") Integer recipeID);

    @GET("/api/v1/recipes/{recipeID}")
    Call<RecipeDetails> getRecipeByID(@Path("recipeID") Integer recipeID);

    @POST("/api/v1/recipes/by-ingredients")
    Call<List<RecipeDetails>> postRecipesByIngredients(@Body Map<String, IngredientDetails> ingredients);
}
