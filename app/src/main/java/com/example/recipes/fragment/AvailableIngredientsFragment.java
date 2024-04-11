package com.example.recipes.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.recipes.R;
import com.example.recipes.models.AvailableIngredient;
import com.example.recipes.mvvm.IngredientViewModel;
import com.example.recipes.recyclerview.AvailableIngredientAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Objects;

public class AvailableIngredientsFragment extends Fragment {

    FloatingActionButton addIngredient;
    RecyclerView ingredientRecyclerView;
    private AvailableIngredientAdapter availableIngredientAdapter;
    private IngredientViewModel ingredientViewModel;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_available_ingredients, container, false);

        addIngredient = view.findViewById(R.id.btnAddIngredient);
        ingredientRecyclerView = view.findViewById(R.id.recyclerViewAvailableIngredients);

        Objects.requireNonNull(getActivity()).setTitle(R.string.app_name);

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_main);

        ingredientRecyclerView.setHasFixedSize(true);
        ingredientRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ingredientViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(Objects.requireNonNull(this.getActivity()).getApplication())).get(IngredientViewModel.class);

        availableIngredientAdapter = new AvailableIngredientAdapter();
        ingredientRecyclerView.setAdapter(availableIngredientAdapter);


        addIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AvailableIngredientsFragmentDirections.ActionAvailableIngredientsFragmentToAddIngredientFragment action = AvailableIngredientsFragmentDirections.actionAvailableIngredientsFragmentToAddIngredientFragment();
                navController.navigate(action);
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
                    ingredientViewModel.delete(availableIngredientAdapter.getIngredient(viewHolder.getAdapterPosition()));
                    Toast.makeText(getContext(), "Ingredient șters!", Toast.LENGTH_SHORT).show();
                } else {

                    int ingredientID=availableIngredientAdapter.getIngredient(viewHolder.getAdapterPosition()).getIngredientID();
                    String name=availableIngredientAdapter.getIngredient(viewHolder.getAdapterPosition()).getName();
                    float quantity=(float) availableIngredientAdapter.getIngredient(viewHolder.getAdapterPosition()).getQuantity();

                    AvailableIngredientsFragmentDirections.ActionAvailableIngredientsFragmentToAddIngredientFragment action = AvailableIngredientsFragmentDirections.actionAvailableIngredientsFragmentToAddIngredientFragment();
                    action.setIngredientID(ingredientID);
                    action.setName(name);
                    action.setQuantity(quantity);
                    navController.navigate(action);
                }
            }
        }).attachToRecyclerView(ingredientRecyclerView);


        ingredientViewModel.getAllIngredients().observe(this, new Observer<List<AvailableIngredient>>() {
            @Override
            public void onChanged(List<AvailableIngredient> availableIngredients) {
                availableIngredientAdapter.submitList(availableIngredients);
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuInflater menuInflater= Objects.requireNonNull(getActivity()).getMenuInflater();
        menuInflater.inflate(R.menu.menu_delete_all,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId()==R.id.deleteAll){
            ingredientViewModel.deleteAll();
            Toast.makeText(getContext(), "Lista a fost golită", Toast.LENGTH_SHORT).show();
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }
}