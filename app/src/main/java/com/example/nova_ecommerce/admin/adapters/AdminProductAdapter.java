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
import com.example.nova_ecommerce.models.Product;

import java.util.List;

public class AdminProductAdapter extends
        RecyclerView.Adapter<AdminProductAdapter.ViewHolder> {

    public interface OnEditClickListener   {
        void onEdit(Product product);
    }
    public interface OnDeleteClickListener {
        void onDelete(Product product);
    }

    private final Context              context;
    private final List<Product>        list;
    private final OnEditClickListener   onEdit;
    private final OnDeleteClickListener onDelete;

    public AdminProductAdapter(Context context,
                               List<Product> list,
                               OnEditClickListener onEdit,
                               OnDeleteClickListener onDelete) {
        this.context  = context;
        this.list     = list;
        this.onEdit   = onEdit;
        this.onDelete = onDelete;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_admin_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product p = list.get(position);

        holder.tvName.setText(p.getName());
        holder.tvPrice.setText("Rs. " + (int) p.getPrice());
        holder.tvCategory.setText(p.getCategoryName());
        holder.tvStock.setText("Stock: " + p.getStock());

        Glide.with(context)
                .load(p.getImageURL())
                .centerCrop()
                .placeholder(R.color.colorPrimary)
                .into(holder.imgProduct);

        holder.btnEdit.setOnClickListener(v   -> onEdit.onEdit(p));
        holder.btnDelete.setOnClickListener(v -> onDelete.onDelete(p));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView   imgProduct;
        TextView    tvName, tvPrice, tvCategory, tvStock;
        ImageButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct  = itemView.findViewById(R.id.imgAdminProduct);
            tvName      = itemView.findViewById(R.id.tvAdminProductName);
            tvPrice     = itemView.findViewById(R.id.tvAdminProductPrice);
            tvCategory  = itemView.findViewById(R.id.tvAdminProductCategory);
            tvStock     = itemView.findViewById(R.id.tvAdminProductStock);
            btnEdit     = itemView.findViewById(R.id.btnEditProduct);
            btnDelete   = itemView.findViewById(R.id.btnDeleteProduct);
        }
    }
}