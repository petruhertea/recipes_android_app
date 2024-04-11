package com.example.recipes.recyclerview;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recipes.R;
import com.example.recipes.models.BeverageDetails;

import java.util.List;

public class BeverageRecyclerAdapter extends RecyclerView.Adapter<BeverageRecyclerAdapter.ViewHolder> {

    private List<BeverageDetails> beverageDetailsList;

    public void setBeverageDetailsList(List<BeverageDetails> beverageDetails) {
        this.beverageDetailsList = beverageDetails;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvBeverageName;
        private final ImageView imgBeverage;

        public TextView getTvBeverageName() {
            return tvBeverageName;
        }

        public ImageView getImgBeverage() {
            return imgBeverage;
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvBeverageName = (TextView) itemView.findViewById(R.id.tvBeverageName);
            imgBeverage = (ImageView) itemView.findViewById(R.id.imgBeverage);
        }
    }

    @NonNull
    @Override
    public BeverageRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_beverages, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BeverageRecyclerAdapter.ViewHolder holder, int position) {
        BeverageDetails beverageDetails = beverageDetailsList.get(position);

        holder.getTvBeverageName().setText(beverageDetails.getBeverageSuggestions());

        byte[] imageBase64 = Base64.decode(beverageDetails.getBeverageImage(), Base64.DEFAULT);

        Glide.with(holder.itemView.getContext()).load(imageBase64).override(150, 150)
                .centerCrop().into(holder.getImgBeverage());

    }

    @Override
    public int getItemCount() {
        return beverageDetailsList.size();
    }
}
