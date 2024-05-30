package com.recipes.retrofit;

import com.recipes.models.BeverageDetails;

import java.util.List;

public interface BeverageDetailsCallback {
    void onBeverageDetailsReceived(List<BeverageDetails> beverageDetails);

    void onFailure(String errorMessage);
}
