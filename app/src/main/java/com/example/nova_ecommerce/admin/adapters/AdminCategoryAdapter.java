package com.example.nova_ecommerce.admin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.user.models.Category;

import java.util.List;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.ViewHolder> {

    public interface OnEditClickListener   {
        void onEdit(Category category);
    }
    public interface OnDeleteClickListener {
        void onDelete(Category category);
    }

    private final Context              context;
    private final List<Category>       list;
    private final OnEditClickListener   onEdit;
    private final OnDeleteClickListener onDelete;

    public AdminCategoryAdapter(Context context, List<Category> list, OnEditClickListener onEdit, OnDeleteClickListener onDelete) {
        this.context  = context;
        this.list     = list;
        this.onEdit   = onEdit;
        this.onDelete = onDelete;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_admin_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category cat = list.get(position);
        holder.tvCatName.setText(cat.getName());

        Glide.with(context)
                .load(cat.getImageURL())
                .centerCrop()
                .placeholder(R.color.colorPrimary)
                .into(holder.imgCategory);

        holder.btnEdit.setOnClickListener(v   -> onEdit.onEdit(cat));
        holder.btnDelete.setOnClickListener(v -> onDelete.onDelete(cat));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView  imgCategory;
        TextView   tvCatName;
        ImageButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCategory = itemView.findViewById(R.id.imgAdminCategory);
            tvCatName   = itemView.findViewById(R.id.tvAdminCatName);
            btnEdit     = itemView.findViewById(R.id.btnEditCategory);
            btnDelete   = itemView.findViewById(R.id.btnDeleteCategory);
        }
    }
}