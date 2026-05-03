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

import java.util.List;

public class SuccessItemAdapter extends
        RecyclerView.Adapter<SuccessItemAdapter.ViewHolder> {

    private final Context      context;
    private final List<String> names, qtys, prices, images;

    public SuccessItemAdapter(Context context,
                              List<String> names,
                              List<String> qtys,
                              List<String> prices,
                              List<String> images) {
        this.context = context;
        this.names   = names;
        this.qtys    = qtys;
        this.prices  = prices;
        this.images  = images;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_checkout_product,
                        parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {
        holder.tvName.setText(names.get(position));
        holder.tvQty.setText("Qty: " + qtys.get(position));

        try {
            double price = Double.parseDouble(prices.get(position));
            int    qty   = Integer.parseInt(qtys.get(position));
            holder.tvPrice.setText("Rs. "
                    + String.format("%,.0f", price * qty));
        } catch (NumberFormatException e) {
            holder.tvPrice.setText("Rs. " + prices.get(position));
        }

        Glide.with(context)
                .load(images.get(position))
                .centerCrop()
                .placeholder(R.color.colorPrimary)
                .into(holder.imgItem);
    }

    @Override
    public int getItemCount() {
        return names != null ? names.size() : 0;
    }

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