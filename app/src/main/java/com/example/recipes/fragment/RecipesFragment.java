package com.example.recipes.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.recipes.R;
import com.example.recipes.models.AvailableIngredient;
import com.example.recipes.models.RecipeDetails;
import com.example.recipes.mvvm.IngredientViewModel;
import com.example.recipes.recyclerview.RecipeRecyclerAdapter;
import com.example.recipes.retrofit.RecipeDetailsCallback;
import com.example.recipes.retrofit.RecipesApi;
import com.example.recipes.retrofit.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipesFragment extends Fragment {

    private List<RecipeDetails> recipeDetailsList = new ArrayList<>();
    IngredientViewModel ingredientViewModel;
    private List<AvailableIngredient> availableIngredientList=new ArrayList<>();

    List<String> ingredientNames = new ArrayList<>();
    Double[] quantities;

    NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_recipes, container, false);

        navController= Navigation.findNavController(requireActivity(),R.id.nav_host_main);

        setupViewModelAndRecyclerView(view);

        return view;


    }

    public void setupViewModelAndRecyclerView(View view){

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewRecipes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        RecipeRecyclerAdapter adapter = new RecipeRecyclerAdapter();


        adapter.setOnRecipeItemClickListener(new RecipeRecyclerAdapter.OnRecipeItemClickListener() {
            @Override
            public void onRecipeItemClick(int position) {
                RecipeDetails clickedRecipe = recipeDetailsList.get(position);

                int recipeID=clickedRecipe.getRecipeID();

                NavDirections action =RecipesFragmentDirections.actionRecipesFragmentToRecipeDetailsFragment(recipeID);
                navController.navigate(action);
            }
        });

        ingredientViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(Objects.requireNonNull(this.getActivity()).getApplication())).get(IngredientViewModel.class);

        ingredientViewModel.getAllIngredients().observe(this, new Observer<List<AvailableIngredient>>() {
            @Override
            public void onChanged(List<AvailableIngredient> availableIngredients) {
                availableIngredientList=availableIngredients;

                for(AvailableIngredient ingredient : availableIngredientList){
                    ingredientNames.add(ingredient.getName());
                    Log.d("Ingredients", ingredient.getName());
                }

                // Extract ingredient quantities from availableIngredientList
                quantities = new Double[availableIngredientList.size()];
                for (int i = 0; i < availableIngredientList.size(); i++) {
                    quantities[i] = availableIngredientList.get(i).getQuantity();
                }


                if (!ingredientNames.isEmpty()){

                    getRecipeDetailsByIngredients(new RecipeDetailsCallback() {
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


                }
                else{
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
                }

            }
        });
    }


    public void getRecipeDetailsByIngredients(RecipeDetailsCallback callback) {
        RecipesApi apiService = RetrofitClient.getClient(getContext()).create(RecipesApi.class);

        Call<List<RecipeDetails>> call = apiService.getRecipesByIngredients(ingredientNames,quantities);
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