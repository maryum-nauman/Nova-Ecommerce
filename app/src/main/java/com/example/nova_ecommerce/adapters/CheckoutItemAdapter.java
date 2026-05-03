package com.example.nova_ecommerce.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.models.CartItem;

import java.util.List;

public class CheckoutItemAdapter extends
        RecyclerView.Adapter<CheckoutItemAdapter.ViewHolder> {

    private final Context        context;
    private final List<CartItem> items;

    public CheckoutItemAdapter(Context context, List<CartItem> items) {
        this.context = context;
        this.items   = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_checkout_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {
        CartItem item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvQty.setText("Qty: " + item.getQuantity());
        holder.tvPrice.setText("Rs. "
                + String.format("%,.0f",
                item.getPrice() * item.getQuantity()));

        Glide.with(context)
                .load(item.getImageUrl())
                .centerCrop()
                .placeholder(R.color.colorPrimary)
                .into(holder.imgItem);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgItem;
        TextView  tvName, tvQty, tvPrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgItem = itemView.findViewById(R.id.imgCheckoutItem);
            tvName  = itemView.findViewById(R.id.tvCheckoutItemName);
            tvQty   = itemView.findViewById(R.id.tvCheckoutItemQty);
            tvPrice = itemView.findViewById(R.id.tvCheckoutItemPrice);
        }
    }
}