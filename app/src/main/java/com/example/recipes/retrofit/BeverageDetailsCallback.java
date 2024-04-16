package com.example.recipes.retrofit;

import com.example.recipes.models.BeverageDetails;

import java.util.List;

public interface BeverageDetailsCallback {
    void onBeverageDetailsReceived(List<BeverageDetails> beverageDetails);

    void onFailure(String errorMessage);
}
