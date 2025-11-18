package com.cookcraft.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cookcraft.R;
import com.google.android.material.snackbar.Snackbar;
import com.cookcraft.databinding.FragmentRecipesBinding;
import com.cookcraft.model.AvailableIngredient;
import com.cookcraft.model.RecipeDetails;
import com.cookcraft.mvvm.IngredientViewModel;
import com.cookcraft.mvvm.RecipeViewModel;
import com.cookcraft.recyclerview.RecipeRecyclerAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;

import java.util.ArrayList;
import java.util.List;

public class RecipesFragment extends Fragment {

    IngredientViewModel ingredientViewModel;
    List<String> ingredientsList = new ArrayList<>();
    NavController navController;
    private FragmentRecipesBinding binding;
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
        binding = null;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentRecipesBinding.inflate(inflater, container, false);

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

        // Create ViewModels
        ingredientViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(IngredientViewModel.class);

        RecipeViewModel recipeViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(RecipeViewModel.class);

        // Clean expired cache on start
        recipeViewModel.cleanExpiredCache();

        // Use switchMap to automatically switch between recipe sources based on ingredients
        LiveData<List<AvailableIngredient>> ingredientsLiveData = ingredientViewModel.getAllIngredients();

        LiveData<List<RecipeDetails>> recipesLiveData = Transformations.switchMap(ingredientsLiveData,
                ingredients -> {
                    if (ingredients != null && !ingredients.isEmpty()) {
                        // Has ingredients: fetch filtered recipes
                        return recipeViewModel.getRecipesByIngredients(ingredients);
                    } else {
                        // No ingredients: fetch all recipes
                        return recipeViewModel.getAllRecipes();
                    }
                });

        // Observe the recipes LiveData (automatically updates when ingredients change)
        recipesLiveData.observe(getViewLifecycleOwner(), new Observer<List<RecipeDetails>>() {
            @Override
            public void onChanged(List<RecipeDetails> recipes) {
                if (recipes != null) {
                    recipeDetailsList = recipes;
                    adapter.setRecipeList(recipeDetailsList);

                    if (binding.recyclerViewRecipes.getAdapter() == null) {
                        binding.recyclerViewRecipes.setAdapter(adapter);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });

        // Observe network status
        recipeViewModel.getNetworkStatus().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isConnected) {
                if (!isConnected) {
                    // Show offline indicator
                    Snackbar.make(
                            binding.getRoot(),
                            "📡 Offline mode - showing cached recipes",
                            Snackbar.LENGTH_SHORT
                    ).show();
                }
            }
        });

        // Setup pull-to-refresh
        binding.swipeRefresh.setColorSchemeResources(
                R.color.salmon_red,
                R.color.vermilion_red
        );

        binding.swipeRefresh.setOnRefreshListener(() -> {
            // Check if we have ingredients or not
            if (availableIngredientList != null && !availableIngredientList.isEmpty()) {
                // Refresh with ingredients
                recipeViewModel.forceRefreshRecipesByIngredients(availableIngredientList)
                        .observe(getViewLifecycleOwner(), recipes -> {
                            binding.swipeRefresh.setRefreshing(false);
                            if (recipes != null) {
                                recipeDetailsList = recipes;
                                adapter.setRecipeList(recipeDetailsList);
                                adapter.notifyDataSetChanged();

                                Snackbar.make(binding.getRoot(),
                                        "✓ Recipes refreshed",
                                        Snackbar.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // Refresh all recipes
                recipeViewModel.forceRefreshAllRecipes()
                        .observe(getViewLifecycleOwner(), recipes -> {
                            binding.swipeRefresh.setRefreshing(false);
                            if (recipes != null) {
                                recipeDetailsList = recipes;
                                adapter.setRecipeList(recipeDetailsList);
                                adapter.notifyDataSetChanged();

                                Snackbar.make(binding.getRoot(),
                                        "✓ Recipes refreshed",
                                        Snackbar.LENGTH_SHORT).show();
                            }
                        });
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

    @Override
    public void onResume() {
        super.onResume();
        binding.adView.resume();
    }
}