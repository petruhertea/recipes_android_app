package com.cookcraft.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cookcraft.R;
import com.cookcraft.models.RecipeDetails;

import java.util.List;

public class RecipeRecyclerAdapter extends RecyclerView.Adapter<RecipeRecyclerAdapter.ViewHolder> {

    private List<RecipeDetails> recipeDetailsList;
    private OnRecipeItemClickListener clickListener;


    public void setRecipeList(List<RecipeDetails> recipeList) {
        this.recipeDetailsList = recipeList;
    }

    public void setOnRecipeItemClickListener(OnRecipeItemClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public RecipeRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeRecyclerAdapter.ViewHolder holder, int position) {
        RecipeDetails recipeDetails = recipeDetailsList.get(position);

        holder.getTvRecipeTitle().setText(recipeDetails.getRecipeTitle());
        holder.getTvRecipeDescription().setText(recipeDetails.getRecipeDescription());

    }

    @Override
    public int getItemCount() {
        return recipeDetailsList.size();
    }

    public interface OnRecipeItemClickListener {
        void onRecipeItemClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView tvRecipeTitle;
        private final TextView tvRecipeDescription;

        public ViewHolder(View view) {
            super(view);
            tvRecipeTitle = (TextView) view.findViewById(R.id.tvRecipeTitle);
            tvRecipeDescription = (TextView) view.findViewById(R.id.tvRecipeDescription);

            view.setContentDescription(tvRecipeTitle.getText().toString());

            view.setOnClickListener(this);

        }

        public TextView getTvRecipeTitle() {
            return tvRecipeTitle;
        }

        public TextView getTvRecipeDescription() {
            return tvRecipeDescription;
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    clickListener.onRecipeItemClick(position);
                }
            }
        }
    }

}


