package com.example.nova_ecommerce.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.database.CartDatabaseHelper;
import com.example.nova_ecommerce.models.CartItem;
import com.example.nova_ecommerce.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class DealAdapter extends
        RecyclerView.Adapter<DealAdapter.DealViewHolder> {

    public interface OnDealClickListener {
        void onDealClick(Product product);
    }

    private final Context             context;
    private final List<Product>       dealList;
    private       OnDealClickListener listener;
    private final CartDatabaseHelper  cartDb;
    private final String              userId;

    public DealAdapter(Context context, List<Product> dealList) {
        this.context  = context;
        this.dealList = dealList;
        this.cartDb   = CartDatabaseHelper.getInstance(context);
        this.userId   = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
    }

    public void setOnDealClickListener(OnDealClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                             int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_deal_card, parent, false);
        return new DealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder,
                                 int position) {
        Product product = dealList.get(position);

        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(product.getFormattedPrice());

        Glide.with(context)
                .load(product.getImageURL())
                .centerCrop()
                .placeholder(R.color.colorPrimary)
                .into(holder.imgProduct);

        // Favorite button
        holder.btnFavorite.setImageResource(
                product.isFavorite()
                        ? R.drawable.ic_favorite
                        : R.drawable.ic_favorite_border);

        holder.btnFavorite.setOnClickListener(v -> {
            boolean nowFav = !product.isFavorite();
            product.setFavorite(nowFav);
            notifyItemChanged(position);
            if (userId == null) return;

            DatabaseReference favRef = FirebaseDatabase.getInstance(
                            "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com"
                    ).getReference("users")
                    .child(userId).child("favorites")
                    .child(product.getId());

            if (nowFav) {
                favRef.setValue(product);
                Toast.makeText(context,
                        "Added to favorites!", Toast.LENGTH_SHORT).show();
            } else {
                favRef.removeValue();
                Toast.makeText(context,
                        "Removed from favorites", Toast.LENGTH_SHORT).show();
            }
        });

        // Add to cart
        holder.btnAddToCart.setOnClickListener(v -> {
            CartItem item = new CartItem(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    product.getImageURL(), 1);
            item.setCategoryId(product.getCategoryId());
            cartDb.addOrIncrement(item,userId);
            Toast.makeText(context,
                    product.getName() + " added to cart!",
                    Toast.LENGTH_SHORT).show();
        });

        // Card click → detail
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDealClick(product);
        });
    }

    @Override
    public int getItemCount() { return dealList.size(); }

    static class DealViewHolder extends RecyclerView.ViewHolder {
        ImageView   imgProduct;
        TextView    tvName, tvPrice, btnAddToCart;
        ImageButton btnFavorite;

        DealViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct  = itemView.findViewById(R.id.imgDealProduct);
            tvName      = itemView.findViewById(R.id.tvDealName);
            tvPrice     = itemView.findViewById(R.id.tvDealPrice);
            btnAddToCart = itemView.findViewById(R.id.btnDealAddCart);
            btnFavorite = itemView.findViewById(R.id.btnDealFavorite);
        }
    }
}