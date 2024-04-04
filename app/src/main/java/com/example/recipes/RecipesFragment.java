package com.example.recipes;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.recipes.models.RecipeDetails;
import com.example.recipes.recyclerview.RecipeRecyclerAdapter;
import com.example.recipes.retrofit.RecipeDetailsCallback;
import com.example.recipes.retrofit.RecipesApi;
import com.example.recipes.retrofit.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipesFragment extends Fragment {

    private List<RecipeDetails> recipeDetailsList = new ArrayList<>();

    NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_recipes, container, false);

        navController= Navigation.findNavController(requireActivity(),R.id.nav_host_main);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewRecipes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        RecipeRecyclerAdapter adapter = new RecipeRecyclerAdapter();

        getRecipeDetails(new RecipeDetailsCallback() {
            @Override
            public void onRecipeDetailsReceived(List<RecipeDetails> recipeDetails) {
                recipeDetailsList=recipeDetails;
                adapter.setRecipeList(recipeDetailsList);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d("Response", errorMessage);
            }
        });

        adapter.setOnRecipeItemClickListener(new RecipeRecyclerAdapter.OnRecipeItemClickListener() {
            @Override
            public void onRecipeItemClick(int position) {
                RecipeDetails clickedRecipe = recipeDetailsList.get(position);

                Integer recipeID=clickedRecipe.getRecipeID();

                NavDirections action =RecipesFragmentDirections.actionRecipesFragmentToRecipeDetailsFragment(recipeID);
                navController.navigate(action);
            }
        });

        return view;


    }

    public void getRecipeDetails(RecipeDetailsCallback callback) {
        RecipesApi apiService = RetrofitClient.getClient(getContext()).create(RecipesApi.class);

        Call<List<RecipeDetails>> call = apiService.getAllRecipes();
        call.enqueue(new Callback<List<RecipeDetails>>() {
            @Override
            public void onResponse(Call<List<RecipeDetails>> call, Response<List<RecipeDetails>> response) {
                if (response.isSuccessful()) {
                    List<RecipeDetails> recipeDetails = response.body();
                    /*
                    for (RecipeDetails recipe : recipeDetails) {
                        Log.d("RecipeTitle", recipe.getRecipeTitle());
                        Log.d("RecipeImage",recipe.getRecipeImage());
                        // Log other properties as needed...
                    }
                    */

                    if (callback != null) {
                        callback.onRecipeDetailsReceived(recipeDetails);
                    }
                } else {
                    if (callback != null) {
                        callback.onFailure("Response unsuccessful");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<RecipeDetails>> call, Throwable t) {
                if (callback != null) {
                    callback.onFailure(t.getMessage());
                }
            }
        });
    }

}