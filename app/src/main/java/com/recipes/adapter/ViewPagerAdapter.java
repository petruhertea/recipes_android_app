package com.recipes.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.recipes.R;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return NavHostFragment.create(R.navigation.nav_graph_ingredients);
            case 1:
                return NavHostFragment.create(R.navigation.nav_graph_recipes);
            default:
                return NavHostFragment.create(R.navigation.nav_graph_ingredients);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
