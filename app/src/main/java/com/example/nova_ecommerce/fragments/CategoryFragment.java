package com.example.nova_ecommerce.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.adapters.CategoryAdapter;
import com.example.nova_ecommerce.adapters.ProductAdapter;
import com.example.nova_ecommerce.models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {

    private RecyclerView recyclerCategories, recyclerProducts;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private List<String> categoryList = new ArrayList<>();
    private List<Product> productList = new ArrayList<>();
    private ProgressBar progressBar;
    private DatabaseReference productsRef, categoriesRef;
    private String selectedCategory = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);

        recyclerCategories = view.findViewById(R.id.recyclerCategories);
        recyclerProducts   = view.findViewById(R.id.recyclerCategoryProducts);
        progressBar        = view.findViewById(R.id.progressBarCat);

        productsRef   = FirebaseDatabase.getInstance().getReference("products");
        categoriesRef = FirebaseDatabase.getInstance().getReference("categories");

        // Horizontal category chips
        recyclerCategories.setLayoutManager(
                new LinearLayoutManager(getContext(),
                        LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(getContext(), categoryList, category -> {
            selectedCategory = category;
            loadProductsByCategory(category);
            categoryAdapter.setSelected(category);
        });
        recyclerCategories.setAdapter(categoryAdapter);

        // 2-column product grid
        recyclerProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productAdapter = new ProductAdapter(getContext(), productList);
        recyclerProducts.setAdapter(productAdapter);

        loadCategories();
        return view;
    }

    private void loadCategories() {
        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                categoryList.add("All");
                for (DataSnapshot child : snapshot.getChildren()) {
                    String name = child.child("name").getValue(String.class);
                    if (name != null) categoryList.add(name);
                }
                categoryAdapter.notifyDataSetChanged();
                categoryAdapter.setSelected("All");
                loadProductsByCategory("All");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),
                        "Error loading categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProductsByCategory(String category) {
        progressBar.setVisibility(View.VISIBLE);

        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Product p = child.getValue(Product.class);
                    if (p != null) {
                        p.setId(child.getKey());
                        if (category.equals("All")
                                || category.equals(p.getCategory())) {
                            productList.add(p);
                        }
                    }
                }
                productAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}