package com.recipes.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayoutMediator;
import com.recipes.R;
import com.recipes.adapter.ViewPagerAdapter;
import com.recipes.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    InterstitialAd mInterstitialAd;

    //AdView adView;

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Force light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding=ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        ViewPagerAdapter adapter= new ViewPagerAdapter(this);

        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setUserInputEnabled(false);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.ingrediente_disponiblie);
                    break;
                case 1:
                    tab.setText(R.string.retete);
                    break;
            }
        }).attach();

        initAdMob();

        loadInterstitialAd();

        CountDownTimer countDownTimer=new CountDownTimer(120000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                loadInterstitialAd();
                start();
            }
        };

        countDownTimer.start();
    }

    private void initAdMob() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                Log.d("AdMob", "Initialized");
            }

        });
    }

    public void showInterstitialAd(){
        if (mInterstitialAd != null) {
            mInterstitialAd.show(this);
        } else {
            Log.d("AdMob", "showInterstitialAd: Ad not loaded");
        }
    }

    public void loadInterstitialAd(){
        AdRequest adRequest=new AdRequest.Builder().build();
        InterstitialAd.load(this, getResources().getString(R.string.interstitial_ad_id), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.d("AdMob","Error: "+loadAdError);
            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                mInterstitialAd=interstitialAd;
                Log.d("AdMob","Loaded");
            }
        });
    }


}