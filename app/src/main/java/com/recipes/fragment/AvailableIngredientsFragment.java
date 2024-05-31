package com.recipes.fragment;

import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.recipes.R;
import com.recipes.databinding.FragmentAvailableIngredientsBinding;
import com.recipes.models.AvailableIngredient;
import com.recipes.mvvm.IngredientViewModel;
import com.recipes.recyclerview.AvailableIngredientAdapter;

import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class AvailableIngredientsFragment extends Fragment {

    private AvailableIngredientAdapter availableIngredientAdapter;
    private IngredientViewModel ingredientViewModel;
    private NavController navController;

    private FragmentAvailableIngredientsBinding binding;

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

        binding=FragmentAvailableIngredientsBinding.inflate(inflater,container,false);

        View view = binding.getRoot();

        loadBannerAd();

        navController = NavHostFragment.findNavController(AvailableIngredientsFragment.this);

        setupViews(view);

        return view;
    }

    private void setupViews(View view){
        binding.recyclerViewAvailableIngredients.setHasFixedSize(true);

        binding.recyclerViewAvailableIngredients.setLayoutManager(new LinearLayoutManager(getContext()));

        ingredientViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.requireActivity().getApplication())).get(IngredientViewModel.class);

        availableIngredientAdapter = new AvailableIngredientAdapter();
        binding.recyclerViewAvailableIngredients.setAdapter(availableIngredientAdapter);

        binding.btnAddIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AvailableIngredientsFragmentDirections.ActionAvailableIngredientsFragmentToAddIngredientFragment action = AvailableIngredientsFragmentDirections.actionAvailableIngredientsFragmentToAddIngredientFragment();
                navController.navigate(action);
            }
        });

        binding.btnDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ingredientViewModel.deleteAll();
                Toast.makeText(getContext(), R.string.empty_list, Toast.LENGTH_SHORT).show();
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.RIGHT) {
                    ingredientViewModel.delete(availableIngredientAdapter.getIngredient(viewHolder.getAbsoluteAdapterPosition()));
                    Toast.makeText(getContext(), R.string.ingredient_deleted, Toast.LENGTH_SHORT).show();
                } else {

                    int ingredientID = availableIngredientAdapter.getIngredient(viewHolder.getAbsoluteAdapterPosition()).getIngredientID();
                    String name = availableIngredientAdapter.getIngredient(viewHolder.getAbsoluteAdapterPosition()).getName();
                    float quantity = (float) availableIngredientAdapter.getIngredient(viewHolder.getAbsoluteAdapterPosition()).getQuantity();
                    String measureUnit = availableIngredientAdapter.getIngredient(viewHolder.getAbsoluteAdapterPosition()).getMeasureUnit();

                    AvailableIngredientsFragmentDirections.ActionAvailableIngredientsFragmentToAddIngredientFragment action = AvailableIngredientsFragmentDirections.actionAvailableIngredientsFragmentToAddIngredientFragment();
                    action.setIngredientID(ingredientID);
                    action.setName(name);
                    action.setQuantity(quantity);
                    action.setMeasureUnit(measureUnit);
                    navController.navigate(action);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_blue))
                        .addSwipeLeftActionIcon(R.drawable.ic_baseline_edit_note_24)
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
                        .addSwipeRightActionIcon(R.drawable.ic_baseline_delete_sweep_24)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(binding.recyclerViewAvailableIngredients);


        ingredientViewModel.getAllIngredients().observe(this, new Observer<List<AvailableIngredient>>() {
            @Override
            public void onChanged(List<AvailableIngredient> availableIngredients) {
                availableIngredientAdapter.submitList(availableIngredients);
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