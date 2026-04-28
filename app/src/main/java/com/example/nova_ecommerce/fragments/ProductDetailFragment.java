package com.example.nova_ecommerce.fragments;

import android.os.Bundle;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.adapters.ProductAdapter;
import com.example.nova_ecommerce.database.CartDatabaseHelper;
import com.example.nova_ecommerce.models.CartItem;
import com.example.nova_ecommerce.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailFragment extends Fragment {

    private static final String ARG_PRODUCT_ID = "productId";

    // Views
    private ImageView imgProduct;
    private ImageButton btnBack, btnFavorite;
    private TextView tvName, tvPrice, tvRating,
            tvReviewCount, tvCategory, tvDescription;
    private Button btnAddToCart, btnBuyNow;
    private RecyclerView recyclerRelated;

    // Data
    private Product currentProduct;
    private final List<Product> relatedList = new ArrayList<>();
    private ProductAdapter relatedAdapter;
    private DatabaseReference productsRef;
    private CartDatabaseHelper cartDb;
    private String userId;

    public static ProductDetailFragment newInstance(String productId) {
        ProductDetailFragment f = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PRODUCT_ID, productId);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_detail,
                container, false);

        // Firebase
        productsRef = FirebaseDatabase.getInstance(
                "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com"
        ).getReference("products");

        cartDb = CartDatabaseHelper.getInstance(getContext());

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // Bind views
        imgProduct     = view.findViewById(R.id.imgProductDetail);
        btnBack        = view.findViewById(R.id.btnBack);
        btnFavorite    = view.findViewById(R.id.btnFavoriteDetail);
        tvName         = view.findViewById(R.id.tvDetailName);
        tvPrice        = view.findViewById(R.id.tvDetailPrice);
        tvRating       = view.findViewById(R.id.tvDetailRating);
        tvReviewCount  = view.findViewById(R.id.tvDetailReviewCount);
        tvCategory     = view.findViewById(R.id.tvDetailCategory);
        tvDescription  = view.findViewById(R.id.tvDetailDescription);
        btnAddToCart   = view.findViewById(R.id.btnDetailAddToCart);
        btnBuyNow      = view.findViewById(R.id.btnBuyNow);
        recyclerRelated = view.findViewById(R.id.recyclerRelated);

        // Related products grid
        recyclerRelated.setLayoutManager(
                new GridLayoutManager(getContext(), 2));
        relatedAdapter = new ProductAdapter(getContext(), relatedList);
        recyclerRelated.setAdapter(relatedAdapter);
        // Disable nested scrolling so NestedScrollView scrolls smoothly
        recyclerRelated.setNestedScrollingEnabled(false);

        // Back button
        btnBack.setOnClickListener(v ->
                getParentFragmentManager().popBackStack());

        // Load product
        String productId = getArguments() != null
                ? getArguments().getString(ARG_PRODUCT_ID) : "";
        loadProduct(productId);

        return view;
    }

    private void loadProduct(String productId) {
        productsRef.child(productId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentProduct = snapshot.getValue(Product.class);
                        if (currentProduct == null) return;
                        currentProduct.setId(snapshot.getKey());
                        bindProduct();
                        loadRelatedProducts(currentProduct.getCategory());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(),
                                "Error loading product",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindProduct() {
        // Image
        Glide.with(this)
                .load(currentProduct.getImageURL())
                .centerCrop()
                .placeholder(R.color.colorPrimary)
                .into(imgProduct);

        // Text fields
        tvName.setText(currentProduct.getName());
        tvPrice.setText(currentProduct.getFormattedPrice());
        tvRating.setText(String.valueOf(currentProduct.getRating()));
        tvReviewCount.setText("(" + currentProduct.getReviewCount()
                + " reviews)");
        tvCategory.setText(currentProduct.getCategory());
        tvDescription.setText(currentProduct.getDescription());

        // Favorite state
        updateFavoriteIcon();

        // Toggle favorite
        btnFavorite.setOnClickListener(v -> {
            boolean nowFav = !currentProduct.isFavorite();
            currentProduct.setFavorite(nowFav);
            updateFavoriteIcon();

            if (userId == null) return;
            DatabaseReference favRef = FirebaseDatabase.getInstance(
                            "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com"
                    ).getReference("users")
                    .child(userId).child("favorites")
                    .child(currentProduct.getId());

            if (nowFav) {
                favRef.setValue(currentProduct);
                Toast.makeText(getContext(),
                        "Added to favorites!", Toast.LENGTH_SHORT).show();
            } else {
                favRef.removeValue();
                Toast.makeText(getContext(),
                        "Removed from favorites", Toast.LENGTH_SHORT).show();
            }
        });

        // Add to cart
        btnAddToCart.setOnClickListener(v -> {
            CartItem item = new CartItem(
                    currentProduct.getId(),
                    currentProduct.getName(),
                    currentProduct.getPrice(),
                    currentProduct.getImageURL(),
                    1
            );
            cartDb.addOrIncrement(item);
            Toast.makeText(getContext(),
                    "Added to cart!", Toast.LENGTH_SHORT).show();
        });

        // Buy now — add to cart then go to checkout
        btnBuyNow.setOnClickListener(v -> {
            CartItem item = new CartItem(
                    currentProduct.getId(),
                    currentProduct.getName(),
                    currentProduct.getPrice(),
                    currentProduct.getImageURL(),
                    1
            );
            cartDb.addOrIncrement(item);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CheckoutFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void updateFavoriteIcon() {
        btnFavorite.setImageResource(
                currentProduct.isFavorite()
                        ? R.drawable.ic_favorite
                        : R.drawable.ic_favorite_border);
    }

    private void loadRelatedProducts(String category) {
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                relatedList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Product p = child.getValue(Product.class);
                    if (p != null && !child.getKey()
                            .equals(currentProduct.getId())
                            && category.equals(p.getCategory())) {
                        p.setId(child.getKey());
                        relatedList.add(p);
                    }
                }
                relatedAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}