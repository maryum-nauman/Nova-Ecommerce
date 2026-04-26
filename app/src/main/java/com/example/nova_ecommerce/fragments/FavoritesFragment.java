package com.example.nova_ecommerce.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    private RecyclerView recyclerFavorites;
    private ProductAdapter adapter;
    private List<Product> favoriteList = new ArrayList<>();
    private TextView tvEmptyFav;
    private DatabaseReference favRef;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        recyclerFavorites = view.findViewById(R.id.recyclerFavorites);
        tvEmptyFav        = view.findViewById(R.id.tvEmptyFav);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            favRef = FirebaseDatabase.getInstance()
                    .getReference("users").child(userId).child("favorites");
        }

        recyclerFavorites.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new ProductAdapter(getContext(), favoriteList);
        recyclerFavorites.setAdapter(adapter);

        adapter.setOnProductClickListener(product -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container,
                            ProductDetailFragment.newInstance(product.getId()))
                    .addToBackStack(null)
                    .commit();
        });

        loadFavorites();
        return view;
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
                        favoriteList.add(product);
                    }
                }
                adapter.notifyDataSetChanged();
                tvEmptyFav.setVisibility(
                        favoriteList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}