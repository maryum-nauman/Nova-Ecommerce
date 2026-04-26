package com.example.nova_ecommerce.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.adapters.ProductAdapter;
import com.example.nova_ecommerce.models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShopFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private DatabaseReference dbRef;

    // productList = what RecyclerView currently shows (changes on search)
    private final List<Product> productList = new ArrayList<>();

    // fullList = ALL products from Firebase, never touched by search
    private final List<Product> fullList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);

        recyclerView = view.findViewById(R.id.recyclerProducts);
        progressBar  = view.findViewById(R.id.progressBar);
        tvEmpty      = view.findViewById(R.id.tvEmpty);

        dbRef = FirebaseDatabase.getInstance(
                "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com"
        ).getReference("products");

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new ProductAdapter(getContext(), productList);
        recyclerView.setAdapter(adapter);

        loadProducts();
        return view;
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                productList.clear();
                fullList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Product product = child.getValue(Product.class);
                    if (product != null) {
                        product.setId(child.getKey());
                        productList.add(product);
                        fullList.add(product);
                    }
                }

                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(
                        productList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(),
                        "Error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Called by Dashboard every time user types in search bar ──
    public void filterProducts(String query) {
        if (adapter == null || fullList.isEmpty()) return;

        productList.clear();

        if (query.isEmpty()) {
            // Empty search → show all products
            productList.addAll(fullList);
            tvEmpty.setVisibility(View.GONE);
        } else {
            String lower = query.toLowerCase();
            for (Product p : fullList) {
                // Match against name, category and description
                if (p.getName() != null
                        && p.getName().toLowerCase().contains(lower)
                        || p.getCategory() != null
                        && p.getCategory().toLowerCase().contains(lower)
                        || p.getDescription() != null
                        && p.getDescription().toLowerCase().contains(lower)) {
                    productList.add(p);
                }
            }
            // Show "No products found" if nothing matches
            tvEmpty.setVisibility(
                    productList.isEmpty() ? View.VISIBLE : View.GONE);
        }

        adapter.notifyDataSetChanged();
    }
}