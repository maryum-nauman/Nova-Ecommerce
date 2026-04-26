package com.example.nova_ecommerce.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nova_ecommerce.R;

import java.util.List;

public class CategoryAdapter extends
        RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    private Context context;
    private List<String> categoryList;
    private OnCategoryClickListener listener;
    private String selectedCategory = "All";

    public CategoryAdapter(Context context, List<String> categoryList,
                           OnCategoryClickListener listener) {
        this.context      = context;
        this.categoryList = categoryList;
        this.listener     = listener;
    }

    public void setSelected(String category) {
        this.selectedCategory = category;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categoryList.get(position);
        holder.tvCategoryName.setText(category);

        // Highlight selected category with teal background
        boolean isSelected = category.equals(selectedCategory);
        holder.cardCategory.setCardBackgroundColor(
                isSelected
                        ? context.getColor(R.color.colorPrimary)
                        : 0xFFFFFFFF
        );

        holder.cardCategory.setOnClickListener(v -> {
            selectedCategory = category;
            notifyDataSetChanged();
            listener.onCategoryClick(category);
        });
    }

    @Override
    public int getItemCount() { return categoryList.size(); }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        CardView  cardCategory;
        ImageView imgCategory;
        TextView  tvCategoryName;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardCategory   = itemView.findViewById(R.id.cardCategory);
            imgCategory    = itemView.findViewById(R.id.imgCategory);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}