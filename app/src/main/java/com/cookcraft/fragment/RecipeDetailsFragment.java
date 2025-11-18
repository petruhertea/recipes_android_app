package com.cookcraft.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cookcraft.R;
import com.cookcraft.activity.MainActivity;
import com.cookcraft.databinding.FragmentRecipeDetailsBinding;
import com.cookcraft.model.BeverageDetails;
import com.cookcraft.model.RecipeDetails;
import com.cookcraft.mvvm.RecipeViewModel;
import com.cookcraft.recyclerview.BeverageRecyclerAdapter;
import com.cookcraft.retrofit.BeverageDetailsCallback;
import com.cookcraft.retrofit.RecipesApi;
import com.cookcraft.retrofit.RetrofitClient;
import com.cookcraft.retrofit.SingleRecipeCallback;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeDetailsFragment extends Fragment {

    NavController navController;
    RecipeDetails recipe;
    private FragmentRecipeDetailsBinding binding;
    private List<BeverageDetails> beverageDetailsList = new ArrayList<>();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        long startTime = System.currentTimeMillis();
        Log.d("RecipeDetails", "Starting to load recipe...");

        navController = NavHostFragment.findNavController(RecipeDetailsFragment.this);

        getRecipeDetails();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewGrid);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        BeverageRecyclerAdapter adapter = new BeverageRecyclerAdapter();

        getBeverageDetails(adapter, recyclerView);

        Log.d("RecipeDetails", "Recipe load initiated in " + (System.currentTimeMillis() - startTime) + "ms");

        return view;
    }

    private void getRecipeDetails() {
        RecipeViewModel recipeViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(RecipeViewModel.class);

        Bundle args = getArguments();
        assert args != null;
        Integer recipeID = args.getInt("recipeID");

        recipeViewModel.getRecipeById(recipeID).observe(getViewLifecycleOwner(), new Observer<RecipeDetails>() {
            @Override
            public void onChanged(RecipeDetails recipeDetails) {
                if (recipeDetails != null) {
                    recipe = recipeDetails;
                    displayRecipeDetails(recipeDetails);
                }
            }
        });
    }

    private void getBeverageDetails(BeverageRecyclerAdapter adapter, RecyclerView recyclerView) {
        RecipeViewModel recipeViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(RecipeViewModel.class);

        Bundle args = getArguments();
        assert args != null;
        Integer recipeID = args.getInt("recipeID");

        recipeViewModel.getBeveragesByRecipeId(recipeID).observe(getViewLifecycleOwner(), new Observer<List<BeverageDetails>>() {
            @Override
            public void onChanged(List<BeverageDetails> beverageDetails) {
                if (beverageDetails != null && !beverageDetails.isEmpty()) {
                    beverageDetailsList = beverageDetails;
                    adapter.setBeverageDetailsList(beverageDetails);
                    recyclerView.setAdapter(adapter);
                }
            }
        });
    }

    // Helper method to display recipe details
    private void displayRecipeDetails(RecipeDetails recipeDetails) {
        String bundleTitle, bundleDescription, bundleInstructions, bundleImage, bundleIngredients;
        int bundleServings, bundleCookTime, bundlePrepTime, bundleTotalTime;

        bundleTitle = recipeDetails.getRecipeTitle();
        bundleDescription = recipeDetails.getRecipeDescription();
        bundleInstructions = recipeDetails.getRecipeInstructions();
        bundleImage = recipeDetails.getRecipeImage();
        bundlePrepTime = recipeDetails.getPrepTimeMinutes();
        bundleCookTime = recipeDetails.getCookTimeMinutes();
        bundleTotalTime = recipeDetails.getTotalTimeMinutes();
        bundleServings = recipeDetails.getServings();
        bundleIngredients = recipeDetails.getIngredients();

        bundleImage = bundleImage.replace("localhost", "10.0.2.2");

        String[] ingredientsArray = bundleIngredients.split(", ");
        StringBuilder ingredientTextBuilder = new StringBuilder();

        for (String ingredient : ingredientsArray) {
            String[] parts = ingredient.split(": ");
            if (parts.length == 2) {
                ingredientTextBuilder.append(" - ").append(parts[0]).append(": ").append(parts[1]).append("\n");
            } else {
                ingredientTextBuilder.append(parts[0]).append(": N/A\n");
            }

        }

        String[] cookingInstructionsArray = bundleInstructions.split("[.] ");
        StringBuilder instructionsTextBuilder = new StringBuilder();

        for (String instruction : cookingInstructionsArray) {
            instructionsTextBuilder.append(" - ").append(instruction).append(".").append("\n");
        }

        String serv, prep, cook, total, recipeTimersString;
        String ingredientsText = getResources().getString(R.string.ingrediente) + ingredientTextBuilder;

        serv = "Nr. de porții: " + bundleServings;
        prep = "Timp pentru pregătire: " + bundlePrepTime + " min";
        cook = "Timp de preparare: " + bundleCookTime + " min";
        total = "Timp total: " + bundleTotalTime + " min";
        recipeTimersString = serv + "\n" + prep + "\n" + cook + "\n" + total;

        String cookDirections = "Mod de preparare:\n" + instructionsTextBuilder;

        binding.tvRecipeTitle.setText(bundleTitle);
        binding.tvRecipeInstructions.setText(cookDirections);
        binding.tvRecipeDescription.setText(bundleDescription);
        binding.tvRecipeTimers.setText(recipeTimersString);
        binding.tvRecipeIngredients.setText(ingredientsText);

        Glide.with(requireContext())
                .load(bundleImage)
                .override(150, 150)
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_question_mark_24)
                .into(binding.imgRecipe);
    }

    @Override
    public void onResume() {
        super.onResume();

        ((MainActivity) requireActivity()).showInterstitialAd();
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navController.popBackStack();
            }
        });
    }

}