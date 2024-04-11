package com.example.recipes.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.recipes.R;
import com.example.recipes.models.AvailableIngredient;
import com.example.recipes.mvvm.IngredientViewModel;

import java.util.Objects;


public class AddIngredientFragment extends Fragment {

    EditText etName, etQuantity;
    Button btnSave;
    IngredientViewModel ingredientViewModel;
    NavController navController;

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navController.popBackStack();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_add_ingredient, container, false);

        navController = Navigation.findNavController(requireActivity(),R.id.nav_host_main);

        etName =view.findViewById(R.id.etName);
        etQuantity =view.findViewById(R.id.etQuantity);
        btnSave=view.findViewById(R.id.btnSave);

        ingredientViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(Objects.requireNonNull(this.getActivity()).getApplication())).get(IngredientViewModel.class);


        Bundle args=getArguments();

        int id=args.getInt("ingredientID");

        if (id!=-1){
            getActivity().setTitle("Modifică ingredient");
            etName.setText(args.getString("name"));
            etQuantity.setText(String.valueOf(args.getFloat("quantity")));
            btnSave.setText(R.string.modifica);
        }
        else{
            getActivity().setTitle("Adaugă ingredient");
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name, quantityText;

                name = etName.getText().toString();
                quantityText=etQuantity.getText().toString();

                if (name.trim().isEmpty() || quantityText.trim().isEmpty()){
                    Toast.makeText(getContext(), "Toate câmpurile trebuie completate!", Toast.LENGTH_SHORT).show();
                    return;
                }

                double quantity=Double.parseDouble(quantityText);

                if (id==-1) {
                    AvailableIngredient availableIngredient = new AvailableIngredient(name, quantity);
                    ingredientViewModel.insert(availableIngredient);
                    navController.popBackStack();
                    Toast.makeText(getContext(), "Ingredient adăugat!", Toast.LENGTH_SHORT).show();
                }else{
                    AvailableIngredient availableIngredient = new AvailableIngredient(name, quantity);
                    availableIngredient.setIngredientID(id);
                    ingredientViewModel.update(availableIngredient);
                    navController.popBackStack();
                    Toast.makeText(getContext(), "Ingredient modificat!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        return view;
    }
}