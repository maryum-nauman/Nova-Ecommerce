package com.example.nova_ecommerce.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private FirebaseFirestore db;
    private CartDatabaseHelper cartDb;
    private String userId;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context     = context;
        this.productList = productList;
        this.db          = FirebaseFirestore.getInstance();
        this.cartDb      = CartDatabaseHelper.getInstance(context);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvProductName.setText(product.getName());
        holder.tvPrice.setText(product.getFormattedPrice());
        holder.tvRating.setText(product.getRating()
                + " (" + product.getReviewCount() + ")");

        Glide.with(context)
                .load(product.getImageURL())
                .placeholder(R.color.colorPrimary)
                .error(R.color.colorPrimary)
                .timeout(15000)
                .into(holder.imgProduct);

        // Favorite icon state
        holder.btnFavorite.setImageResource(product.isFavorite()
                ? R.drawable.ic_favorite
                : R.drawable.ic_favorite);

        // Toggle favorite in Realtime Database
        holder.btnFavorite.setOnClickListener(v -> {
            boolean nowFav = !product.isFavorite();
            product.setFavorite(nowFav);
            notifyItemChanged(position);

            if (userId == null) return;

            DatabaseReference favRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("favorites")
                    .child(product.getId());

            if (nowFav) {
                favRef.setValue(product)
                        .addOnSuccessListener(unused ->
                                Toast.makeText(context,
                                        "Added to favorites!",
                                        Toast.LENGTH_SHORT).show());
            } else {
                favRef.removeValue()
                        .addOnSuccessListener(unused ->
                                Toast.makeText(context,
                                        "Removed from favorites",
                                        Toast.LENGTH_SHORT).show());
            }
        });
        // Add to cart — saved in SQLite locally
        holder.btnAddToCart.setOnClickListener(v -> {
            CartItem item = new CartItem(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    product.getImageURL(),
                    1
            );
            cartDb.addOrIncrement(item);
            Toast.makeText(context,
                    product.getName() + " added to cart!",
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() { return productList.size(); }

    public void filter(String query, List<Product> fullList) {
        productList.clear();
        if (query.isEmpty()) {
            productList.addAll(fullList);
        } else {
            String lower = query.toLowerCase();
            for (Product p : fullList) {
                if (p.getName().toLowerCase().contains(lower)
                        || p.getCategory().toLowerCase().contains(lower)) {
                    productList.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView   imgProduct;
        ImageButton btnFavorite;
        TextView    tvProductName, tvPrice, tvRating;
        Button      btnAddToCart;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct    = itemView.findViewById(R.id.imgProduct);
            btnFavorite   = itemView.findViewById(R.id.btnFavorite);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice       = itemView.findViewById(R.id.tvPrice);
            tvRating      = itemView.findViewById(R.id.tvRating);
            btnAddToCart  = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}