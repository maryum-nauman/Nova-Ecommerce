package com.example.nova_ecommerce.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.adapters.ProductAdapter;
import com.example.nova_ecommerce.adapters.ReviewAdapter;
import com.example.nova_ecommerce.database.CartDatabaseHelper;
import com.example.nova_ecommerce.models.CartItem;
import com.example.nova_ecommerce.models.Product;
import com.example.nova_ecommerce.models.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductDetailFragment extends Fragment {

    private static final String ADMIN_UID        = "48ULkpPhYfVOAAfqcKbD7VtXOyt1";
    private static final String ARG_PRODUCT_ID   = "productId";
    private static final String ARG_CATEGORY_ID  = "categoryId";

    // ── Views ─────────────────────────────────────────────────
    private ImageView    imgProduct;
    private ImageButton  btnBack, btnFavorite;
    private TextView     tvName, tvPrice, tvRating,
            tvReviewCount, tvCategory, tvDescription;
    private RatingBar    ratingBarDetail;
    private Button       btnAddToCart, btnBuyNow, btnWriteReview;
    private RecyclerView recyclerRelated, recyclerReviews;
    private TextView     tvNoReviews;

    // ── Data ──────────────────────────────────────────────────
    private Product            currentProduct;
    private final List<Product> relatedList  = new ArrayList<>();
    private final List<Review>  reviewList   = new ArrayList<>();
    private ProductAdapter     relatedAdapter;
    private ReviewAdapter      reviewAdapter;
    private CartDatabaseHelper cartDb;
    private String             userId;
    private String             userName;
    private String             categoryId;

    public static ProductDetailFragment newInstance(String productId,
                                                    String categoryId) {
        ProductDetailFragment f = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PRODUCT_ID,  productId);
        args.putString(ARG_CATEGORY_ID, categoryId);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_product_detail, container, false);

        cartDb = CartDatabaseHelper.getInstance(getContext());

        // Get current user info
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance()
                    .getCurrentUser().getUid();
            fetchUserName(); // load name from DB for reviews
        }

        // ── Bind views ────────────────────────────────────────
        imgProduct      = view.findViewById(R.id.imgProductDetail);
        btnBack         = view.findViewById(R.id.btnBack);
        btnFavorite     = view.findViewById(R.id.btnFavoriteDetail);
        tvName          = view.findViewById(R.id.tvDetailName);
        tvPrice         = view.findViewById(R.id.tvDetailPrice);
        tvRating        = view.findViewById(R.id.tvDetailRating);
        tvReviewCount   = view.findViewById(R.id.tvDetailReviewCount);
        tvCategory      = view.findViewById(R.id.tvDetailCategory);
        tvDescription   = view.findViewById(R.id.tvDetailDescription);
        ratingBarDetail = view.findViewById(R.id.ratingBarDetail);
        btnAddToCart    = view.findViewById(R.id.btnDetailAddToCart);
        btnBuyNow       = view.findViewById(R.id.btnBuyNow);
        btnWriteReview  = view.findViewById(R.id.btnWriteReview);
        recyclerRelated = view.findViewById(R.id.recyclerRelated);
        recyclerReviews = view.findViewById(R.id.recyclerReviews);
        tvNoReviews     = view.findViewById(R.id.tvNoReviews);

        // Related products setup
        recyclerRelated.setLayoutManager(
                new GridLayoutManager(getContext(), 2));
        relatedAdapter = new ProductAdapter(getContext(), relatedList);
        recyclerRelated.setAdapter(relatedAdapter);
        recyclerRelated.setNestedScrollingEnabled(false);

        // Reviews setup
        recyclerReviews.setLayoutManager(
                new LinearLayoutManager(getContext()));
        reviewAdapter = new ReviewAdapter(getContext(), reviewList);
        recyclerReviews.setAdapter(reviewAdapter);
        recyclerReviews.setNestedScrollingEnabled(false);

        btnBack.setOnClickListener(v ->
                getParentFragmentManager().popBackStack());

        btnWriteReview.setOnClickListener(v -> showWriteReviewDialog());

        if (getArguments() != null) {
            categoryId       = getArguments().getString(ARG_CATEGORY_ID);
            String productId = getArguments().getString(ARG_PRODUCT_ID);
            loadProduct(productId, categoryId);
        }

        return view;
    }

    // ── Fetch the logged-in user's display name ───────────────
    private void fetchUserName() {
        FirebaseDatabase.getInstance(
                        "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com"
                ).getReference("users").child(userId).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        userName = snap.getValue(String.class);
                        if (userName == null) userName = "Anonymous";
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                        userName = "Anonymous";
                    }
                });
    }

    // ── Load product from Firebase ────────────────────────────
    private void loadProduct(String productId, String catId) {
        DatabaseReference productRef = FirebaseDatabase.getInstance(
                        "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com"
                ).getReference("products")
                .child(ADMIN_UID).child(catId)
                .child("items").child(productId);

        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentProduct = snapshot.getValue(Product.class);
                if (currentProduct == null) return;
                currentProduct.setId(snapshot.getKey());
                currentProduct.setCategoryId(catId);

                // Fetch category name
                FirebaseDatabase.getInstance(
                                "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com"
                        ).getReference("products")
                        .child(ADMIN_UID).child(catId).child("name")
                        .addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(
                                            @NonNull DataSnapshot s) {
                                        String n = s.getValue(String.class);
                                        if (n != null)
                                            currentProduct.setCategoryName(n);
                                        bindProduct();
                                        loadRelatedProducts(catId);
                                        loadReviews(productId, catId);
                                    }
                                    @Override
                                    public void onCancelled(
                                            @NonNull DatabaseError e) {
                                        bindProduct();
                                    }
                                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),
                        "Error loading product",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Bind product data to views ────────────────────────────
    private void bindProduct() {
        Glide.with(this)
                .load(currentProduct.getImageURL())
                .centerCrop()
                .placeholder(R.color.colorPrimary)
                .into(imgProduct);

        tvName.setText(currentProduct.getName());
        tvPrice.setText(currentProduct.getFormattedPrice());
        tvRating.setText(String.valueOf(currentProduct.getRating()));
        tvReviewCount.setText("(" + currentProduct.getReviewCount()
                + " reviews)");
        tvCategory.setText(currentProduct.getCategoryName());
        tvDescription.setText(currentProduct.getDescription());
        ratingBarDetail.setRating((float) currentProduct.getRating());

        updateFavoriteIcon();

        // Favorite toggle
        btnFavorite.setOnClickListener(v -> {

            // TEMP DEBUG — remove after fixing
            android.util.Log.d("FAV_DEBUG", "=== FAVORITE TAPPED ===");
            android.util.Log.d("FAV_DEBUG", "userId = " + userId);
            android.util.Log.d("FAV_DEBUG", "productId = " + (currentProduct != null ? currentProduct.getId() : "NULL PRODUCT"));
            android.util.Log.d("FAV_DEBUG", "isFav = " + (currentProduct != null ? currentProduct.isFavorite() : "N/A"));
            // ... your existing code continues unchanged below

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
                Map<String, Object> favData = new HashMap<>();
                favData.put("name",         currentProduct.getName());
                favData.put("price",        currentProduct.getPrice());
                favData.put("imageURL",     currentProduct.getImageURL());
                favData.put("description",  currentProduct.getDescription());
                favData.put("rating",       currentProduct.getRating());
                favData.put("reviewCount",  currentProduct.getReviewCount());
                favData.put("stock",        currentProduct.getStock());
                favData.put("categoryId",   currentProduct.getCategoryId());    // ← critical
                favData.put("categoryName", currentProduct.getCategoryName());
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
                    currentProduct.getImageURL(), 1);
            item.setCategoryId(currentProduct.getCategoryId());
            cartDb.addOrIncrement(item,userId);
            Toast.makeText(getContext(),
                    "Added to cart!", Toast.LENGTH_SHORT).show();
        });

        // Buy now
        btnBuyNow.setOnClickListener(v -> {
            CartItem item = new CartItem(
                    currentProduct.getId(),
                    currentProduct.getName(),
                    currentProduct.getPrice(),
                    currentProduct.getImageURL(), 1);
            item.setCategoryId(currentProduct.getCategoryId());
            cartDb.addOrIncrement(item,userId);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container,
                            new CheckoutFragment())
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

    // ── Load reviews from Firebase ────────────────────────────
    private void loadReviews(String productId, String catId) {
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance(
                        "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com"
                ).getReference("products")
                .child(ADMIN_UID).child(catId)
                .child("items").child(productId)
                .child("reviews");

        reviewsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Review review = child.getValue(Review.class);
                    if (review != null) {
                        review.setReviewId(child.getKey());
                        reviewList.add(review);
                    }
                }
                reviewAdapter.notifyDataSetChanged();
                tvNoReviews.setVisibility(
                        reviewList.isEmpty()
                                ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ── Write review dialog ───────────────────────────────────
    private void showWriteReviewDialog() {
        if (userId == null) {
            Toast.makeText(getContext(),
                    "Please log in to write a review",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentProduct == null) return;

        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_write_review, null);

        RatingBar rbDialog = dialogView.findViewById(R.id.rbDialogRating);
        EditText  etComment = dialogView.findViewById(R.id.etReviewComment);

        new AlertDialog.Builder(getContext())
                .setTitle("Write a Review")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    float rating  = rbDialog.getRating();
                    String comment = etComment.getText().toString().trim();

                    if (rating == 0) {
                        Toast.makeText(getContext(),
                                "Please give a star rating",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (comment.isEmpty()) {
                        Toast.makeText(getContext(),
                                "Please write a comment",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    submitReview(rating, comment);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Submit review to Firebase ─────────────────────────────
    private void submitReview(float rating, String comment) {
        String timestamp = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date());

        DatabaseReference db = FirebaseDatabase.getInstance(
                "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com"
        ).getReference();

        // Generate a shared review ID
        String reviewId = db.child("products")
                .child(ADMIN_UID)
                .child(currentProduct.getCategoryId())
                .child("items")
                .child(currentProduct.getId())
                .child("reviews").push().getKey();

        // ── 1. Save under product ─────────────────────────────
        Map<String, Object> productReview = new HashMap<>();
        productReview.put("reviewId",   reviewId);
        productReview.put("userId",     userId);
        productReview.put("userName",   userName != null
                ? userName : "Anonymous");
        productReview.put("rating",     rating);
        productReview.put("comment",    comment);
        productReview.put("timestamp",  timestamp);

        db.child("products")
                .child(ADMIN_UID)
                .child(currentProduct.getCategoryId())
                .child("items")
                .child(currentProduct.getId())
                .child("reviews")
                .child(reviewId)
                .setValue(productReview);

        // ── 2. Save under user (includes product info) ────────
        Map<String, Object> userReview = new HashMap<>();
        userReview.put("reviewId",    reviewId);
        userReview.put("productId",   currentProduct.getId());
        userReview.put("productName", currentProduct.getName());
        userReview.put("categoryId",  currentProduct.getCategoryId());
        userReview.put("rating",      rating);
        userReview.put("comment",     comment);
        userReview.put("timestamp",   timestamp);

        db.child("users").child(userId)
                .child("reviews").child(reviewId)
                .setValue(userReview)
                .addOnSuccessListener(unused ->
                        Toast.makeText(getContext(),
                                "Review submitted! Thank you 🎉",
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    // ── Load related products ─────────────────────────────────
    private void loadRelatedProducts(String catId) {
        FirebaseDatabase.getInstance(
                        "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com"
                ).getReference("products")
                .child(ADMIN_UID).child(catId).child("items")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        relatedList.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            if (child.getKey().equals(
                                    currentProduct.getId())) continue;
                            Product p = child.getValue(Product.class);
                            if (p != null) {
                                p.setId(child.getKey());
                                p.setCategoryId(catId);
                                p.setCategoryName(
                                        currentProduct.getCategoryName());
                                relatedList.add(p);
                            }
                        }
                        relatedAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {}
                });
    }
}