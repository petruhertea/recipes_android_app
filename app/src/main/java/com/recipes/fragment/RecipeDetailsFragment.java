package com.recipes.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.recipes.R;
import com.recipes.activity.MainActivity;
import com.recipes.databinding.FragmentRecipeDetailsBinding;
import com.recipes.models.BeverageDetails;
import com.recipes.models.RecipeDetails;
import com.recipes.recyclerview.BeverageRecyclerAdapter;
import com.recipes.retrofit.BeverageDetailsCallback;
import com.recipes.retrofit.RecipesApi;
import com.recipes.retrofit.RetrofitClient;
import com.recipes.retrofit.SingleRecipeCallback;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeDetailsFragment extends Fragment {

    private FragmentRecipeDetailsBinding binding;

    NavController navController;

    RecipeDetails recipe;


    private List<BeverageDetails> beverageDetailsList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding=FragmentRecipeDetailsBinding.inflate(inflater,container,false);

        View view= binding.getRoot();

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_main);

        getRecipeDetails();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewGrid);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        BeverageRecyclerAdapter adapter = new BeverageRecyclerAdapter();

        getBeverageDetails(adapter, recyclerView);
        return view;
    }

    private void getBeverageDetails(BeverageRecyclerAdapter adapter, RecyclerView recyclerView){
        getBeverageDetails(new BeverageDetailsCallback() {

            @Override
            public void onBeverageDetailsReceived(List<BeverageDetails> beverageDetails) {
                beverageDetailsList = beverageDetails;
                adapter.setBeverageDetailsList(beverageDetails);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onFailure(String errorMessage) {

            }
        });
    }

    private void getRecipeDetails() {
        getRecipeDetails(new SingleRecipeCallback() {
            @Override
            public void onRecipeDetailsReceived(RecipeDetails recipeDetails) {
                recipe = recipeDetails;

                String bundleTitle, bundleDescription, bundleInstructions, bundleImage, bundleIngredients;
                int bundleServings, bundleCookTime, bundlePrepTime, bundleTotalTime, bundleID;

                bundleTitle = recipe.getRecipeTitle();
                bundleDescription = recipe.getRecipeDescription();
                bundleInstructions = recipe.getRecipeInstructions();
                bundleImage = recipe.getRecipeImage();
                bundlePrepTime = recipe.getPrepTimeMinutes();
                bundleCookTime = recipe.getCookTimeMinutes();
                bundleTotalTime = recipe.getTotalTimeMinutes();
                bundleServings = recipe.getServings();
                bundleIngredients = recipe.getIngredients();

                String[] ingredientsArray = bundleIngredients.split(", ");

                // Create a map to store ingredients and their quantities
                // Split the ingredient string based on comma delimiter

                // Create a StringBuilder to build the ingredient text
                StringBuilder ingredientTextBuilder = new StringBuilder();

                // Iterate through the ingredients array
                for (String ingredient : ingredientsArray) {
                    // Split each ingredient into name and quantity parts based on colon delimiter
                    String[] parts = ingredient.split(": ");

                    // Append the ingredient name and quantity to the StringBuilder
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
                        .override(150, 150) // Resize the image to 150x150 pixels
                        .centerCrop() // Crop the image if necessary
                        .placeholder(R.drawable.ic_baseline_question_mark_24)
                        .into(binding.imgRecipe);
            }

            @Override
            public void onFailure(String errorMessage) {

            }
        });
    }

    public void getRecipeDetails(SingleRecipeCallback callback) {
        RecipesApi apiService = RetrofitClient.getClient(getContext()).create(RecipesApi.class);

        Bundle args = getArguments();

        assert args != null;
        Integer recipeID = args.getInt("recipeID");

        Call<RecipeDetails> call = apiService.getRecipeByID(recipeID);
        call.enqueue(new Callback<RecipeDetails>() {
            @Override
            public void onResponse(Call<RecipeDetails> call, Response<RecipeDetails> response) {
                if (response.isSuccessful()) {
                    RecipeDetails recipeDetails = response.body();
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
            public void onFailure(Call<RecipeDetails> call, Throwable t) {
                if (callback != null) {
                    callback.onFailure(t.getMessage());
                }
            }
        });
    }

    public void getBeverageDetails(BeverageDetailsCallback callback) {
        RecipesApi apiService = RetrofitClient.getClient(getContext()).create(RecipesApi.class);

        Bundle args = getArguments();

        assert args != null;
        Integer recipeID = args.getInt("recipeID");
        Call<List<BeverageDetails>> call = apiService.getAllSuggestions(recipeID);
        call.enqueue(new Callback<List<BeverageDetails>>() {
            @Override
            public void onResponse(Call<List<BeverageDetails>> call, Response<List<BeverageDetails>> response) {
                if (response.isSuccessful()) {
                    List<BeverageDetails> beverageDetails = response.body();

                    for (BeverageDetails beverage : beverageDetails) {
                        Log.d("BeverageName", beverage.getBeverageSuggestions());
                        // Log other properties as needed...
                    }

                    if (callback != null) {
                        callback.onBeverageDetailsReceived(beverageDetails);
                    }
                } else {
                    if (callback != null) {
                        callback.onFailure("Response unsuccessful");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<BeverageDetails>> call, Throwable t) {
                if (callback != null) {
                    callback.onFailure(t.getMessage());
                }
            }
        });
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