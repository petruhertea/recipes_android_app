package com.example.recipes.mvvm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.recipes.models.AvailableIngredient;

import java.util.List;

public class IngredientViewModel extends AndroidViewModel {

    private IngredientRepository ingredientRepository;
    private LiveData<List<AvailableIngredient>> availableIngredients;

    public IngredientViewModel(@NonNull Application application) {
        super(application);

        ingredientRepository = new IngredientRepository(application);
        availableIngredients = ingredientRepository.getAllData();
    }

    public void insert(AvailableIngredient availableIngredient) {
        ingredientRepository.insertData(availableIngredient);
    }

    public void delete(AvailableIngredient availableIngredient) {
        ingredientRepository.deleteData(availableIngredient);
    }

    public void update(AvailableIngredient availableIngredient) {
        ingredientRepository.updateData(availableIngredient);
        ;
    }

    public void deleteAll() {
        ingredientRepository.deleteAllData();
        ;
    }

    public LiveData<List<AvailableIngredient>> getAllIngredients() {
        return availableIngredients;
    }
}
