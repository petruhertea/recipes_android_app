package com.example.recipes;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.recipes.models.BeverageDetails;
import com.example.recipes.models.RecipeDetails;
import com.example.recipes.recyclerview.BeverageRecyclerAdapter;
import com.example.recipes.retrofit.BeverageDetailsCallback;
import com.example.recipes.retrofit.RecipesApi;
import com.example.recipes.retrofit.RetrofitClient;
import com.example.recipes.retrofit.SingleRecipeCallback;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeDetailsFragment extends Fragment {

    NavController navController;

    TextView title, instructions, description, servings, cookTime, prepTime, totalTime, ingredients;
    ImageView image;

    RecipeDetails recipe;

    private List<BeverageDetails> beverageDetailsList=new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_recipe_details, container, false);

        navController = Navigation.findNavController(requireActivity(),R.id.nav_host_main);

        title = view.findViewById(R.id.tvRecipeTitle);
        instructions = view.findViewById(R.id.tvRecipeInstructions);
        description = view.findViewById(R.id.tvRecipeDescription);
        servings = view.findViewById(R.id.tvNoOfServings);
        cookTime = view.findViewById(R.id.tvCookTime);
        prepTime = view.findViewById(R.id.tvPrepTime);
        totalTime = view.findViewById(R.id.tvTotalTime);
        ingredients = view.findViewById(R.id.tvRecipeIngredients);
        image = view.findViewById(R.id.imgRecipe);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewGrid);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        BeverageRecyclerAdapter adapter = new BeverageRecyclerAdapter();

        getBeverageDetails(new BeverageDetailsCallback() {

            @Override
            public void onBeverageDetailsReceived(List<BeverageDetails> beverageDetails) {
                beverageDetailsList=beverageDetails;
                adapter.setBeverageDetailsList(beverageDetails);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onFailure(String errorMessage) {

            }
        });

        getRecipeDetails(new SingleRecipeCallback() {
            @Override
            public void onRecipeDetailsReceived(RecipeDetails recipeDetails) {
                recipe=recipeDetails;

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

                String[] cookingInstructionsArray=bundleInstructions.split("[.] ");

                StringBuilder instructionsTextBuilder=new StringBuilder();

                for(String instruction:cookingInstructionsArray){
                    instructionsTextBuilder.append(" - ").append(instruction).append(".").append("\n");
                }

                String serv, prep, cook, total;
                String ingredientsText = getResources().getString(R.string.ingrediente) + ingredientTextBuilder;

                serv = "Nr. de porții: " + bundleServings;
                prep = "Timp pentru pregătire: " + bundlePrepTime+" min";
                cook = "Timp de preparare: " + bundleCookTime+" min";
                total = "Timp total: " + bundleTotalTime+" min";

                String cookDirections="Mod de preparare:\n"+instructionsTextBuilder;

                title.setText(bundleTitle);
                instructions.setText(cookDirections);
                description.setText(bundleDescription);
                servings.setText(serv);
                cookTime.setText(cook);
                prepTime.setText(prep);
                totalTime.setText(total);
                ingredients.setText(ingredientsText);

                byte[] imageBase64;

                if(bundleImage!=null){
                    imageBase64= Base64.decode(bundleImage, Base64.DEFAULT);
                }
                else{
                    imageBase64=null;
                }
                Glide.with(getContext())
                        .load(imageBase64)
                        .override(150, 150) // Resize the image to 150x150 pixels
                        .centerCrop() // Crop the image if necessary
                        .placeholder(R.drawable.ic_baseline_question_mark_24)
                        .into(image);
            }

            @Override
            public void onFailure(String errorMessage) {

            }
        });


        return view;
    }

    public void getRecipeDetails(SingleRecipeCallback callback) {
        RecipesApi apiService = RetrofitClient.getClient(getContext()).create(RecipesApi.class);

        Bundle args=getArguments();

        assert args != null;
        Integer recipeID=args.getInt("recipeID");

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

        /*
        Intent intent = getIntent();
        int bundleID = intent.getIntExtra("recipe_id", 0);
        */

        Bundle args=getArguments();

        assert args != null;
        Integer recipeID=args.getInt("recipeID");
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
        // Set up the back button listener
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navController.popBackStack();
            }
        });
    }


}