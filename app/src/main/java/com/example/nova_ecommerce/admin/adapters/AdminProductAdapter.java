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
import com.example.nova_ecommerce.user.models.Product;

import java.util.List;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ViewHolder> {

    public interface OnEditClickListener   { void onEdit(Product product); }
    public interface OnDeleteClickListener { void onDelete(Product product); }
    public interface OnFeaturedToggleListener {
        void onToggle(Product product, boolean newFeaturedState);
    }

    private final Context                 context;
    private final List<Product>           list;
    private final OnEditClickListener     onEdit;
    private final OnDeleteClickListener   onDelete;
    private final OnFeaturedToggleListener onFeaturedToggle;

    public AdminProductAdapter(Context context, List<Product> list, OnEditClickListener onEdit, OnDeleteClickListener onDelete, OnFeaturedToggleListener onFeaturedToggle) {
        this.context          = context;
        this.list             = list;
        this.onEdit           = onEdit;
        this.onDelete         = onDelete;
        this.onFeaturedToggle = onFeaturedToggle;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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

        holder.tvFeaturedBadge.setVisibility(p.isFeatured() ? View.VISIBLE : View.GONE);

        holder.btnToggleFeatured.setImageResource(p.isFeatured() ? R.drawable.ic_star : R.drawable.ic_star_border);

        holder.btnToggleFeatured.setOnClickListener(v -> {
            boolean newState = !p.isFeatured();
            p.setFeatured(newState);
            notifyItemChanged(position);
            onFeaturedToggle.onToggle(p, newState);
        });

        holder.btnEdit.setOnClickListener(v   -> onEdit.onEdit(p));
        holder.btnDelete.setOnClickListener(v -> onDelete.onDelete(p));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView   imgProduct;
        TextView    tvName, tvPrice, tvCategory, tvStock, tvFeaturedBadge;
        ImageButton btnToggleFeatured, btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct        = itemView.findViewById(R.id.imgAdminProduct);
            tvName            = itemView.findViewById(R.id.tvAdminProductName);
            tvPrice           = itemView.findViewById(R.id.tvAdminProductPrice);
            tvCategory        = itemView.findViewById(R.id.tvAdminProductCategory);
            tvStock           = itemView.findViewById(R.id.tvAdminProductStock);
            tvFeaturedBadge   = itemView.findViewById(R.id.tvFeaturedBadge);
            btnToggleFeatured = itemView.findViewById(R.id.btnToggleFeatured);
            btnEdit           = itemView.findViewById(R.id.btnEditProduct);
            btnDelete         = itemView.findViewById(R.id.btnDeleteProduct);
        }
    }
}