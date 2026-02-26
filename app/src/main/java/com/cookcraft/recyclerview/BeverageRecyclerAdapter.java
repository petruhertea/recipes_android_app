package com.cookcraft.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cookcraft.R;
import com.cookcraft.model.BeverageDetails;

/**
 * Adapter for the horizontal beverage suggestions list on the recipe detail screen.
 *
 * Uses DiffUtil + ListAdapter for efficient updates (replaces the old
 * manual setBeverageDetailsList + notifyDataSetChanged pattern).
 */
public class BeverageRecyclerAdapter extends ListAdapter<BeverageDetails,
        BeverageRecyclerAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<BeverageDetails> DIFF =
            new DiffUtil.ItemCallback<BeverageDetails>() {
                @Override
                public boolean areItemsTheSame(@NonNull BeverageDetails o,
                                               @NonNull BeverageDetails n) {
                    return o.getId() == n.getId();
                }
                @Override
                public boolean areContentsTheSame(@NonNull BeverageDetails o,
                                                  @NonNull BeverageDetails n) {
                    return o.getId() == n.getId()
                            && safeEqual(o.getName(), n.getName())
                            && safeEqual(o.getBeverageImage(), n.getBeverageImage());
                }
                private boolean safeEqual(String a, String b) {
                    return a == null ? b == null : a.equals(b);
                }
            };

    public BeverageRecyclerAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_beverages, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BeverageDetails bev = getItem(position);

        // Previously "beverageSuggestions" — now simply "name"
        holder.tvBeverageName.setText(bev.getName());

        String imageUrl = bev.getBeverageImage();
        if (imageUrl != null) {
            imageUrl = imageUrl.replace("localhost", "10.0.2.2");
        }

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .override(150, 150)
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_question_mark_24)
                .into(holder.imgBeverage);

        holder.itemView.setContentDescription(bev.getName());
    }

    // ─── ViewHolder ───────────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvBeverageName;
        final ImageView imgBeverage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBeverageName = itemView.findViewById(R.id.tvBeverageName);
            imgBeverage    = itemView.findViewById(R.id.imgBeverage);
        }
    }
}