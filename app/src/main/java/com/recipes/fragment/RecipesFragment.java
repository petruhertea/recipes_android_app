package com.recipes.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.recipes.R;
import com.recipes.databinding.FragmentRecipesBinding;
import com.recipes.models.AvailableIngredient;
import com.recipes.models.IngredientDetails;
import com.recipes.models.RecipeDetails;
import com.recipes.mvvm.IngredientViewModel;
import com.recipes.recyclerview.RecipeRecyclerAdapter;
import com.recipes.retrofit.RecipeDetailsCallback;
import com.recipes.retrofit.RecipesApi;
import com.recipes.retrofit.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipesFragment extends Fragment {

    private FragmentRecipesBinding binding;

    IngredientViewModel ingredientViewModel;
    List<String> ingredientsList = new ArrayList<>();
    NavController navController;

    private List<RecipeDetails> recipeDetailsList = new ArrayList<>();
    private List<AvailableIngredient> availableIngredientList = new ArrayList<>();

    @Override
    public void onPause() {
        super.onPause();
        binding.adView.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.adView.destroy();
        binding=null;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding=FragmentRecipesBinding.inflate(inflater,container,false);

        View view = binding.getRoot();

        loadBannerAd();

        navController = NavHostFragment.findNavController(RecipesFragment.this);


        setupViewModelAndRecyclerView(view);

        return view;


    }


    public void setupViewModelAndRecyclerView(View view) {


        binding.recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(getContext()));

        RecipeRecyclerAdapter adapter = new RecipeRecyclerAdapter();


        adapter.setOnRecipeItemClickListener(new RecipeRecyclerAdapter.OnRecipeItemClickListener() {
            @Override
            public void onRecipeItemClick(int position) {
                RecipeDetails clickedRecipe = recipeDetailsList.get(position);

                int recipeID = clickedRecipe.getRecipeID();

                NavDirections action = RecipesFragmentDirections.actionRecipesFragmentToRecipeDetailsFragment(recipeID);
                navController.navigate(action);
            }
        });

        ingredientViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.requireActivity().getApplication())).get(IngredientViewModel.class);

        ingredientViewModel.getAllIngredients().observe(getViewLifecycleOwner(), new Observer<List<AvailableIngredient>>() {
            @Override
            public void onChanged(List<AvailableIngredient> availableIngredients) {
                availableIngredientList = availableIngredients;
                ingredientsList.clear();

                for (AvailableIngredient ingredient : availableIngredientList) {
                    String formattedIngredient = ingredient.getName() + "=" + ingredient.getQuantity() + " " + ingredient.getMeasureUnit();
                    ingredientsList.add(formattedIngredient);
                }


                if (!ingredientsList.isEmpty()) {
                    getRecipeDetailsByIngredients(new RecipeDetailsCallback() {
                        @Override
                        public void onRecipeDetailsReceived(List<RecipeDetails> recipeDetails) {
                            recipeDetailsList = recipeDetails;
                            adapter.setRecipeList(recipeDetailsList);
                            binding.recyclerViewRecipes.setAdapter(adapter);
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Log.d("Response", errorMessage);
                        }
                    });


                } else {
                    getRecipeDetails(new RecipeDetailsCallback() {
                        @Override
                        public void onRecipeDetailsReceived(List<RecipeDetails> recipeDetails) {
                            recipeDetailsList = recipeDetails;
                            adapter.setRecipeList(recipeDetailsList);
                            binding.recyclerViewRecipes.setAdapter(adapter);
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

    private void loadBannerAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        binding.adView.loadAd(adRequest);

        binding.adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d("AdMob", "Loaded");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.d("AdMob", loadAdError.getMessage());
            }
        });
    }

    public void getRecipeDetailsByIngredients(RecipeDetailsCallback callback) {
        RecipesApi apiService = RetrofitClient.getClient(getContext()).create(RecipesApi.class);

        // Construct a map from the availableIngredientList
        Map<String, IngredientDetails> ingredientMap = new HashMap<>();
        for (AvailableIngredient ingredient : availableIngredientList) {
            ingredientMap.put(ingredient.getName(), new IngredientDetails(ingredient.getQuantity(), ingredient.getMeasureUnit()));
        }

        Call<List<RecipeDetails>> call = apiService.postRecipesByIngredients(ingredientMap);
        call.enqueue(new Callback<List<RecipeDetails>>() {
            @Override
            public void onResponse(Call<List<RecipeDetails>> call, Response<List<RecipeDetails>> response) {
                if (response.isSuccessful()) {
                    List<RecipeDetails> recipeDetails = response.body();
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

    @Override
    public void onResume() {
        super.onResume();
        binding.adView.resume();
    }
}