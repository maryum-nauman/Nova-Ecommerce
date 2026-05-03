package com.example.nova_ecommerce.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.nova_ecommerce.models.Order;
import com.example.nova_ecommerce.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProfileFragment extends Fragment {

    private static final String DB_URL = "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com";
    private static final String ADMIN_UID = "48ULkpPhYfVOAAfqcKbD7VtXOyt1";

    private ImageView imgAvatar;
    private TextView  tvUserName, tvUserEmail;
    private TextView  tvOrderCount, tvFavCount, tvCartCount, tvReviewCountProfile;
    private TextView  tvPendingCount, tvShippedCount, tvDeliveredCount;
    private View      layoutRecommended;
    private RecyclerView rvRecommended;

    private String userId;
    private DatabaseReference userRef;
    private ProductAdapter recommendedAdapter;
    private final List<Product> recommendedList = new ArrayList<>();
    private final Set<String> addedProductIds = new HashSet<>();

    private final androidx.activity.result.ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) uploadProfileImage(imageUri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imgAvatar = view.findViewById(R.id.imgAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvOrderCount = view.findViewById(R.id.tvOrderCount);
        tvFavCount = view.findViewById(R.id.tvFavCount);
        tvCartCount = view.findViewById(R.id.tvCartCount);
        tvReviewCountProfile = view.findViewById(R.id.tvReviewCountProfile);
        tvPendingCount = view.findViewById(R.id.tvPendingCount);
        tvShippedCount = view.findViewById(R.id.tvShippedCount);
        tvDeliveredCount = view.findViewById(R.id.tvDeliveredCount);
        layoutRecommended = view.findViewById(R.id.layoutRecommended);
        rvRecommended = view.findViewById(R.id.rvRecommendedProfile);

        setupRecommendedRecyclerView();

        // Nav listeners
        View.OnClickListener goToOrders = v -> navigateTo(OrdersFragment.newInstance("all"));
        view.findViewById(R.id.tvViewAllOrders).setOnClickListener(goToOrders);
        view.findViewById(R.id.layoutStatOrders).setOnClickListener(goToOrders);
        view.findViewById(R.id.layoutStatFavs).setOnClickListener(v -> navigateTo(new FavoritesFragment()));
        view.findViewById(R.id.layoutStatCart).setOnClickListener(v -> navigateTo(new CartFragment()));
        view.findViewById(R.id.layoutStatReviews).setOnClickListener(v -> navigateTo(new UserReviewsFragment()));
        view.findViewById(R.id.layoutPending).setOnClickListener(v -> navigateTo(OrdersFragment.newInstance("Pending")));
        view.findViewById(R.id.layoutShipped).setOnClickListener(v -> navigateTo(OrdersFragment.newInstance("Shipped")));
        view.findViewById(R.id.layoutDelivered).setOnClickListener(v -> navigateTo(OrdersFragment.newInstance("Delivered")));

        view.findViewById(R.id.btnEditAvatar).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            userRef = FirebaseDatabase.getInstance(DB_URL).getReference("users").child(userId);
            tvUserEmail.setText(user.getEmail());
            loadProfile();
            loadCounts();
            loadOrderStatusCounts();
            loadRecommendations();
        }

        return view;
    }

    private void setupRecommendedRecyclerView() {
        rvRecommended.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recommendedAdapter = new ProductAdapter(getContext(), recommendedList);
        recommendedAdapter.setOnProductClickListener(product -> 
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, ProductDetailFragment.newInstance(product.getId(), product.getCategoryId()))
                .addToBackStack(null)
                .commit());
        rvRecommended.setAdapter(recommendedAdapter);
    }

    private void loadRecommendations() {
        if (userId == null) return;
        userRef.child("orders").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> orderedCategories = new HashSet<>();
                Set<String> orderedProductIds = new HashSet<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Order order = ds.getValue(Order.class);
                    if (order != null && order.getItems() != null) {
                        for (CartItem item : order.getItems()) {
                            if (item.getCategoryId() != null) orderedCategories.add(item.getCategoryId());
                            orderedProductIds.add(item.getProductId());
                        }
                    }
                }
                if (!orderedCategories.isEmpty()) fetchRelatedProducts(orderedCategories, orderedProductIds);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchRelatedProducts(Set<String> categories, Set<String> excludeIds) {
        DatabaseReference prodRef = FirebaseDatabase.getInstance(DB_URL).getReference("products").child(ADMIN_UID);
        recommendedList.clear();
        addedProductIds.clear();

        for (String catId : categories) {
            prodRef.child(catId).child("items").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String id = ds.getKey();
                        if (excludeIds.contains(id) || addedProductIds.contains(id)) continue;
                        
                        Product p = ds.getValue(Product.class);
                        if (p != null) {
                            p.setId(id);
                            p.setCategoryId(catId);
                            if (recommendedList.size() < 6) {
                                recommendedList.add(p);
                                addedProductIds.add(id);
                            }
                        }
                    }
                    if (!recommendedList.isEmpty()) {
                        layoutRecommended.setVisibility(View.VISIBLE);
                        recommendedAdapter.notifyDataSetChanged();
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void navigateTo(Fragment f) {
        getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, f).addToBackStack(null).commit();
    }

    private void loadProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                tvUserName.setText(name != null && !name.isEmpty() ? name : FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0]);
                String img = snapshot.child("profileImage").getValue(String.class);
                if (img != null && !img.isEmpty() && !img.contains("placeholder")) {
                    Glide.with(requireContext()).load(img).circleCrop().placeholder(R.drawable.ic_person).into(imgAvatar);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadCounts() {
        userRef.child("favorites").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) { tvFavCount.setText(String.valueOf(s.getChildrenCount())); }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        userRef.child("orders").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) { tvOrderCount.setText(String.valueOf(s.getChildrenCount())); }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        userRef.child("reviews").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) { tvReviewCountProfile.setText(String.valueOf(s.getChildrenCount())); }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        tvCartCount.setText(String.valueOf(CartDatabaseHelper.getInstance(requireContext()).getAllItems(userId).size()));
    }

    private void loadOrderStatusCounts() {
        userRef.child("orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int p = 0, s = 0, d = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String status = ds.child("status").getValue(String.class);
                    if (status == null) continue;
                    if (status.equalsIgnoreCase("pending")) p++;
                    else if (status.equalsIgnoreCase("shipped")) s++;
                    else if (status.equalsIgnoreCase("delivered")) d++;
                }
                setBadge(tvPendingCount, p); setBadge(tvShippedCount, s); setBadge(tvDeliveredCount, d);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void setBadge(TextView b, int c) {
        if (c > 0) { b.setText(String.valueOf(c)); b.setVisibility(View.VISIBLE); }
        else b.setVisibility(View.GONE);
    }

    private void uploadProfileImage(Uri uri) {
        StorageReference ref = FirebaseStorage.getInstance().getReference("profile_images/" + userId + ".jpg");
        ref.putFile(uri).addOnSuccessListener(task -> ref.getDownloadUrl().addOnSuccessListener(url -> {
            userRef.child("profileImage").setValue(url.toString());
            Glide.with(requireContext()).load(url).circleCrop().into(imgAvatar);
        }));
    }
}
