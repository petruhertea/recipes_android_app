package com.recipes.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.recipes.R;
import com.recipes.databinding.FragmentAddIngredientBinding;
import com.recipes.models.AvailableIngredient;
import com.recipes.mvvm.IngredientViewModel;

import java.util.Objects;


public class AddIngredientFragment extends Fragment {

    IngredientViewModel ingredientViewModel;
    NavController navController;

    String measureUnit;

    private FragmentAddIngredientBinding binding;

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
    public void onDestroyView() {
        super.onDestroyView();
        binding=null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentAddIngredientBinding.inflate(inflater, container, false);

        View view = binding.getRoot();

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_main);

        ingredientViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(Objects.requireNonNull(this.getActivity()).getApplication())).get(IngredientViewModel.class);

        setupViews(view);

        return view;
    }

    private void setupViews(View view){

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.unit_masura, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.measureUnitSpinner.setAdapter(adapter);
        binding.measureUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                measureUnit = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Bundle args = getArguments();

        assert args != null;
        int id = args.getInt("ingredientID");

        if (id != -1) {
            binding.etName.setText(args.getString("name"));
            binding.etQuantity.setText(String.valueOf(args.getFloat("quantity")));
            measureUnit = args.getString("measureUnit");
            int spinnerPosition = adapter.getPosition(measureUnit);
            binding.measureUnitSpinner.setSelection(spinnerPosition);

            if (measureUnit == null) {
                binding.measureUnitSpinner.setSelection(0);
            }

            binding.btnSave.setText(R.string.modifica);
        } else {
            getActivity().setTitle("Adaugă ingredient");
        }

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name, quantityText;

                name = binding.etName.getText().toString();
                quantityText = binding.etQuantity.getText().toString();

                if (name.trim().isEmpty() || quantityText.trim().isEmpty()) {
                    Toast.makeText(getContext(), "Toate câmpurile trebuie completate!", Toast.LENGTH_SHORT).show();
                    return;
                }

                double quantity = Double.parseDouble(quantityText);

                if (id == -1) {
                    AvailableIngredient availableIngredient = new AvailableIngredient(name, quantity, measureUnit);
                    ingredientViewModel.insert(availableIngredient);
                    navController.popBackStack();
                    Toast.makeText(getContext(), "Ingredient adăugat!", Toast.LENGTH_SHORT).show();
                } else {
                    AvailableIngredient availableIngredient = new AvailableIngredient(name, quantity, measureUnit);
                    availableIngredient.setIngredientID(id);
                    ingredientViewModel.update(availableIngredient);
                    navController.popBackStack();
                    Toast.makeText(getContext(), "Ingredient modificat!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}