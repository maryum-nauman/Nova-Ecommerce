package com.example.nova_ecommerce.user.adapters;

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
import com.example.nova_ecommerce.user.models.Category;

import java.util.List;

public class CategoryAdapter extends
        RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    private final Context context;
    private final List<Category> categoryList;
    private final OnCategoryClickListener listener;
    private String selectedCategoryId = "All";

    public CategoryAdapter(Context context, List<Category> categoryList,
                           OnCategoryClickListener listener) {
        this.context      = context;
        this.categoryList = categoryList;
        this.listener     = listener;
    }

    public void setSelected(String categoryId) {
        this.selectedCategoryId = categoryId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                 int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder,
                                 int position) {
        Category category = categoryList.get(position);
        holder.tvCategoryName.setText(category.getName());

        // Load category image
        Glide.with(context)
                .load(category.getImageURL())
                .centerCrop()
                .placeholder(R.color.colorPrimary)
                .into(holder.imgCategory);

        // Highlight selected
        boolean isSelected = category.getId().equals(selectedCategoryId);
        holder.cardCategory.setCardBackgroundColor(
                isSelected
                        ? context.getColor(R.color.colorPrimary)
                        : 0xFFFFFFFF
        );

        holder.cardCategory.setOnClickListener(v -> {
            selectedCategoryId = category.getId();
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