package com.cookcraft.retrofit;

import com.cookcraft.model.BeverageDetails;

import java.util.List;

public interface BeverageDetailsCallback {
    void onBeverageDetailsReceived(List<BeverageDetails> beverageDetails);

    void onFailure(String errorMessage);
}
