package com.cookcraft.retrofit;

import com.cookcraft.models.BeverageDetails;
import com.cookcraft.models.IngredientDetails;
import com.cookcraft.models.RecipeDetails;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RecipesApi {
    @GET("/api/recipes")
    Call<List<RecipeDetails>> getAllRecipes();

    @GET("/api/recipes/beverages/{recipeID}")
    Call<List<BeverageDetails>> getAllSuggestions(@Path("recipeID") Integer recipeID);

    @GET("/api/recipes/{recipeID}")
    Call<RecipeDetails> getRecipeByID(@Path("recipeID") Integer recipeID);

    @POST("/api/recipes/byIngredients")
    Call<List<RecipeDetails>> postRecipesByIngredients(@Body Map<String, IngredientDetails> ingredients);
}
