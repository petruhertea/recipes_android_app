package com.example.recipes.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.R;
import com.example.recipes.models.AvailableIngredient;

public class AvailableIngredientAdapter extends ListAdapter<AvailableIngredient, AvailableIngredientAdapter.IngredientViewHolder> {


    public static final DiffUtil.ItemCallback<AvailableIngredient> DIFF_CALLBACK = new DiffUtil.ItemCallback<AvailableIngredient>() {
        @Override
        public boolean areItemsTheSame(@NonNull AvailableIngredient oldItem, @NonNull AvailableIngredient newItem) {
            return oldItem.getIngredientID() == newItem.getIngredientID();
        }

        @Override
        public boolean areContentsTheSame(@NonNull AvailableIngredient oldItem, @NonNull AvailableIngredient newItem) {
            return oldItem.getName().equals(newItem.getName()) && oldItem.getQuantity() == newItem.getQuantity();
        }
    };


    public AvailableIngredientAdapter() {
        super(DIFF_CALLBACK);

    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.available_recipe_item, parent, false);
        return new IngredientViewHolder(view);
    }

    public AvailableIngredient getIngredient(int position) {
        return getItem(position);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        AvailableIngredient availableIngredient = getItem(position);
        holder.name.setText(availableIngredient.getName());
        holder.quantity.setText(String.valueOf(availableIngredient.getQuantity()));
        String txtUnit = " " + availableIngredient.getMeasureUnit();
        holder.unit.setText(txtUnit);
    }

    public static class IngredientViewHolder extends RecyclerView.ViewHolder {

        TextView name, quantity, unit;
        ImageView edit, delete;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.tvIngredientName);
            quantity = itemView.findViewById(R.id.tvQuantity);
            edit = itemView.findViewById(R.id.edit);
            delete = itemView.findViewById(R.id.delete);
            unit = itemView.findViewById(R.id.tvMeasureUnit);
        }
    }
}
