package com.example.nova_ecommerce.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.adapters.ProductAdapter;
import com.example.nova_ecommerce.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private static final String ADMIN_UID =
            "48ULkpPhYfVOAAfqcKbD7VtXOyt1";

    private RecyclerView   recyclerFavorites;
    private ProductAdapter adapter;
    private View           layoutEmptyFav;
    private DatabaseReference favRef;

    private final List<Product> favoriteList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_favorites, container, false);

        recyclerFavorites = view.findViewById(R.id.recyclerFavorites);
        layoutEmptyFav    = view.findViewById(R.id.layoutEmptyFav);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance()
                    .getCurrentUser().getUid();
            favRef = FirebaseDatabase.getInstance(
                    "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com"
            ).getReference("users").child(userId).child("favorites");
        }

        recyclerFavorites.setLayoutManager(
                new GridLayoutManager(getContext(), 2));
        adapter = new ProductAdapter(getContext(), favoriteList);
        recyclerFavorites.setAdapter(adapter);

        adapter.setOnProductClickListener(
                product -> navigateToDetail(product));

        loadFavorites();
        return view;
    }

    private void navigateToDetail(Product product) {
        String catId     = product.getCategoryId();
        String productId = product.getId();

        Log.d("FAV_NAV",
                "productId=" + productId + " | categoryId=" + catId);

        if (catId != null && !catId.isEmpty()) {
            // categoryId is already known — navigate directly
            openDetail(productId, catId);
        } else {
            // categoryId missing — search all categories for this product
            findCategoryAndOpen(productId);
        }
    }

    // ── Direct navigation ─────────────────────────────────────
    private void openDetail(String productId, String catId) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container,
                        ProductDetailFragment.newInstance(
                                productId, catId))
                .addToBackStack(null)
                .commit();
    }

    // ── Fallback: scan all categories to find the product ─────
    private void findCategoryAndOpen(String productId) {
        Toast.makeText(getContext(),
                "Loading...", Toast.LENGTH_SHORT).show();

        DatabaseReference adminRef = FirebaseDatabase.getInstance(
                "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com"
        ).getReference("products").child(ADMIN_UID);

        adminRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot adminSnap) {

                        for (DataSnapshot catSnap
                                : adminSnap.getChildren()) {
                            String catId = catSnap.getKey();

                            // Check if this productId exists under items
                            if (catSnap.child("items")
                                    .hasChild(productId)) {
                                Log.d("FAV_NAV",
                                        "Found productId=" + productId
                                                + " in catId=" + catId);

                                // Also update the favorite in DB
                                // so next time categoryId is available
                                if (favRef != null) {
                                    favRef.child(productId)
                                            .child("categoryId")
                                            .setValue(catId);
                                }

                                openDetail(productId, catId);
                                return;
                            }
                        }

                        // Product not found in any category
                        Log.e("FAV_NAV",
                                "productId=" + productId
                                        + " not found in any category");
                        Toast.makeText(getContext(),
                                "Product no longer available",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {
                        Toast.makeText(getContext(),
                                "Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadFavorites() {
        if (favRef == null) return;

        favRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Product product = child.getValue(Product.class);
                    if (product != null) {
                        product.setId(child.getKey());
                        product.setFavorite(true);

                        // Log what categoryId came back
                        Log.d("FAV_LOAD",
                                "id=" + child.getKey()
                                        + " | categoryId="
                                        + product.getCategoryId());

                        favoriteList.add(product);
                    }
                }

                adapter.notifyDataSetChanged();
                layoutEmptyFav.setVisibility(
                        favoriteList.isEmpty()
                                ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}