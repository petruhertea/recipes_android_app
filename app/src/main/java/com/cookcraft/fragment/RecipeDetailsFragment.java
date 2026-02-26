package com.cookcraft.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.cookcraft.R;
import com.cookcraft.activity.MainActivity;
import com.cookcraft.databinding.FragmentRecipeDetailsBinding;
import com.cookcraft.model.RecipeDetails;
import com.cookcraft.mvvm.RecipeViewModel;
import com.cookcraft.recyclerview.BeverageRecyclerAdapter;

import java.util.List;

public class RecipeDetailsFragment extends Fragment {

    private NavController navController;
    private FragmentRecipeDetailsBinding binding;
    private BeverageRecyclerAdapter beverageAdapter;

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

        navController = NavHostFragment.findNavController(this);

        // Set up the horizontal beverage RecyclerView once
        beverageAdapter = new BeverageRecyclerAdapter();
        binding.recyclerViewGrid.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewGrid.setAdapter(beverageAdapter);

        int recipeID = getArguments() != null ? getArguments().getInt("recipeID") : -1;

        RecipeViewModel vm = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(RecipeViewModel.class);

        // Observe recipe details
        vm.getRecipeById(recipeID).observe(getViewLifecycleOwner(), recipe -> {
            if (recipe != null) displayRecipeDetails(recipe);
        });

        // Observe beverage suggestions — adapter handles diffs automatically
        vm.getBeveragesByRecipeId(recipeID).observe(getViewLifecycleOwner(), beverages -> {
            if (beverages != null) beverageAdapter.submitList(beverages);
        });

        return view;
    }

    // ─── Display ─────────────────────────────────────────────────────────

    private void displayRecipeDetails(RecipeDetails recipe) {
        String imageUrl = recipe.getRecipeImage();
        if (imageUrl != null) imageUrl = imageUrl.replace("localhost", "10.0.2.2");

        binding.tvRecipeTitle.setText(recipe.getRecipeTitle());
        binding.tvRecipeDescription.setText(recipe.getRecipeDescription());
        binding.tvRecipeTimers.setText(buildTimersText(recipe));
        binding.tvRecipeIngredients.setText(buildIngredientsText(recipe));
        binding.tvRecipeInstructions.setText(buildInstructionsText(recipe));

        Glide.with(requireContext())
                .load(imageUrl)
                .override(150, 150)
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_question_mark_24)
                .into(binding.imgRecipe);
    }

    /** Builds the timers block: servings, prep, cook, total. */
    private String buildTimersText(RecipeDetails r) {
        return "Nr. de porții: " + r.getServings() + "\n"
                + "Timp pentru pregătire: " + r.getPrepTimeMinutes() + " min\n"
                + "Timp de preparare: " + r.getCookTimeMinutes() + " min\n"
                + "Timp total: " + r.getTotalTimeMinutes() + " min";
    }

    /**
     * Builds the ingredients block from the structured list.
     * No more CSV string splitting — each IngredientLine already has name/qty/unit.
     */
    private String buildIngredientsText(RecipeDetails r) {
        List<RecipeDetails.IngredientLine> lines = r.getIngredients();
        StringBuilder sb = new StringBuilder(getString(R.string.ingrediente));
        if (lines != null) {
            for (RecipeDetails.IngredientLine line : lines) {
                sb.append(" - ").append(line.getName()).append(": ");
                sb.append(line.getQuantity());
                if (line.getUnit() != null && !line.getUnit().isBlank()) {
                    sb.append(" ").append(line.getUnit());
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /** Splits instructions on sentence boundaries and adds bullet markers. */
    private String buildInstructionsText(RecipeDetails r) {
        String instructions = r.getRecipeInstructions();
        if (instructions == null || instructions.isBlank()) return "";

        String[] sentences = instructions.split("[.] ");
        StringBuilder sb = new StringBuilder("Mod de preparare:\n");
        for (String sentence : sentences) {
            sb.append(" - ").append(sentence.trim()).append(".\n");
        }
        return sb.toString();
    }

    // ─── Lifecycle ───────────────────────────────────────────────────────

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).showInterstitialAd();

        requireActivity().getOnBackPressedDispatcher()
                .addCallback(this, new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        navController.popBackStack();
                    }
                });
    }
}