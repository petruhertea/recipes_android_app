package com.example.recipes.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.recipes.R;
import com.example.recipes.models.RecipeDetails;
import com.example.recipes.recyclerview.RecipeRecyclerAdapter;
import com.example.recipes.retrofit.RecipeDetailsCallback;
import com.example.recipes.retrofit.RecipesApi;
import com.example.recipes.retrofit.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity{

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Force light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment=(NavHostFragment)getSupportFragmentManager().findFragmentById(R.id.nav_host_main);
        assert navHostFragment != null;
        navController=navHostFragment.getNavController();

        BottomNavigationView bottomNavigationView=findViewById(R.id.bottom_nav);

        NavigationUI.setupWithNavController(bottomNavigationView,navController);
    }

}