package com.example.nova_ecommerce.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.database.CartDatabaseHelper;
import com.example.nova_ecommerce.models.CartItem;
import com.example.nova_ecommerce.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    // ── Click listener interface ──────────────────────────────
    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private final Context context;
    private final List<Product> productList;
    private final CartDatabaseHelper cartDb;
    private String userId;
    private OnProductClickListener listener;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context     = context;
        this.productList = productList;
        this.cartDb      = CartDatabaseHelper.getInstance(context);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.userId = FirebaseAuth.getInstance()
                    .getCurrentUser().getUid();
        }
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder,
                                 int position) {
        Product product = productList.get(position);

        // ── Basic info ────────────────────────────────────────
        holder.tvProductName.setText(product.getName());
        holder.tvPrice.setText(product.getFormattedPrice());
        holder.tvRating.setText(product.getRating()
                + " (" + product.getReviewCount() + ")");

        // ── Image ─────────────────────────────────────────────
        holder.imgLoadingBg.setVisibility(View.VISIBLE);
        Glide.with(context)
                .load(product.getImageURL())
                .centerCrop()
                .override(400, 300)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(
                            @Nullable GlideException e,
                            Object model,
                            Target<Drawable> target,
                            boolean isFirstResource) {
                        Log.e("GLIDE_ERROR",
                                "Failed: " + product.getImageURL());
                        holder.imgLoadingBg.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(
                            Drawable resource,
                            Object model,
                            Target<Drawable> target,
                            DataSource dataSource,
                            boolean isFirstResource) {
                        holder.imgLoadingBg.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.imgProduct);

        // ── Add to cart ───────────────────────────────────────
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

        // ── Open product detail on card click ─────────────────
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(product);
        });
    }

    @Override
    public int getItemCount() { return productList.size(); }

    // ── Search filter ─────────────────────────────────────────
    public void filter(String query, List<Product> fullList) {
        productList.clear();
        if (query.isEmpty()) {
            productList.addAll(fullList);
        } else {
            String lower = query.toLowerCase();
            for (Product p : fullList) {
                if ((p.getName() != null
                        && p.getName().toLowerCase().contains(lower))
                        || (p.getCategory() != null
                        && p.getCategory().toLowerCase().contains(lower))
                        || (p.getDescription() != null
                        && p.getDescription().toLowerCase().contains(lower))) {
                    productList.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }

    // ── ViewHolder ────────────────────────────────────────────
    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView   imgProduct;
        View        imgLoadingBg;
        ImageButton btnFavorite;
        TextView    tvProductName, tvPrice, tvRating;
        Button      btnAddToCart;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct    = itemView.findViewById(R.id.imgProduct);
            imgLoadingBg  = itemView.findViewById(R.id.imgLoadingBg);
            btnFavorite   = itemView.findViewById(R.id.btnFavorite);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice       = itemView.findViewById(R.id.tvPrice);
            tvRating      = itemView.findViewById(R.id.tvRating);
            btnAddToCart  = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}