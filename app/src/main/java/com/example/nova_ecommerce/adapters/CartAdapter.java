package com.example.nova_ecommerce.adapters;

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
import com.example.nova_ecommerce.database.CartDatabaseHelper;
import com.example.nova_ecommerce.models.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartList;
    private CartDatabaseHelper cartDb;
    private Runnable onCartChanged; // callback to refresh total in fragment

    public CartAdapter(Context context, List<CartItem> cartList,
                       Runnable onCartChanged) {
        this.context       = context;
        this.cartList      = cartList;
        this.cartDb        = CartDatabaseHelper.getInstance(context);
        this.onCartChanged = onCartChanged;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartList.get(position);

        holder.tvCartItemName.setText(item.getName());
        holder.tvCartItemPrice.setText(item.getFormattedPrice());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.color.colorPrimary)
                .into(holder.imgCartItem);

        // Increase quantity
        holder.btnIncrease.setOnClickListener(v -> {
            int newQty = item.getQuantity() + 1;
            item.setQuantity(newQty);
            cartDb.updateQuantity(item.getDocId(), newQty);
            notifyItemChanged(position);
            onCartChanged.run();
        });

        // Decrease quantity
        holder.btnDecrease.setOnClickListener(v -> {
            int newQty = item.getQuantity() - 1;
            if (newQty <= 0) {
                // Remove item
                cartDb.deleteItem(item.getDocId());
                cartList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cartList.size());
            } else {
                item.setQuantity(newQty);
                cartDb.updateQuantity(item.getDocId(), newQty);
                notifyItemChanged(position);
            }
            onCartChanged.run();
        });

        // Delete item
        holder.btnDeleteItem.setOnClickListener(v -> {
            cartDb.deleteItem(item.getDocId());
            cartList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartList.size());
            onCartChanged.run();
        });
    }

    @Override
    public int getItemCount() { return cartList.size(); }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView   imgCartItem;
        TextView    tvCartItemName, tvCartItemPrice, tvQuantity;
        android.widget.Button btnIncrease, btnDecrease;
        ImageButton btnDeleteItem;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCartItem     = itemView.findViewById(R.id.imgCartItem);
            tvCartItemName  = itemView.findViewById(R.id.tvCartItemName);
            tvCartItemPrice = itemView.findViewById(R.id.tvCartItemPrice);
            tvQuantity      = itemView.findViewById(R.id.tvQuantity);
            btnIncrease     = itemView.findViewById(R.id.btnIncrease);
            btnDecrease     = itemView.findViewById(R.id.btnDecrease);
            btnDeleteItem   = itemView.findViewById(R.id.btnDeleteItem);
        }
    }
}