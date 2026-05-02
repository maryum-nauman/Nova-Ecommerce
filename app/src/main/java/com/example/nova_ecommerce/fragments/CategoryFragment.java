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
import com.example.nova_ecommerce.models.Category;
import com.example.nova_ecommerce.models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {

    private static final String ADMIN_UID =
            "48ULkpPhYfVOAAfqcKbD7VtXOyt1";

    private RecyclerView    recyclerCategories, recyclerProducts;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter  productAdapter;

    // ── Now List<Category> instead of List<String> ────────────
    private final List<Category> categoryList = new ArrayList<>();
    private final List<Product>  productList  = new ArrayList<>();

    private ProgressBar      progressBar;
    private DatabaseReference adminRef;

    // "all" is a sentinel id meaning show everything
    private String selectedCategoryId = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_category, container, false);

        recyclerCategories = view.findViewById(R.id.recyclerCategories);
        recyclerProducts   = view.findViewById(R.id.recyclerCategoryProducts);
        progressBar        = view.findViewById(R.id.progressBarCat);

        // Single reference to admin node
        adminRef = FirebaseDatabase.getInstance(
                "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com"
        ).getReference("products").child(ADMIN_UID);

        // ── Horizontal category chips ─────────────────────────
        recyclerCategories.setLayoutManager(
                new LinearLayoutManager(getContext(),
                        LinearLayoutManager.HORIZONTAL, false));

        categoryAdapter = new CategoryAdapter(
                getContext(), categoryList, category -> {
            selectedCategoryId = category.getId();
            categoryAdapter.setSelected(category.getId());
            loadProductsByCategory(category.getId());
        });
        recyclerCategories.setAdapter(categoryAdapter);

        // ── 2-column product grid ─────────────────────────────
        recyclerProducts.setLayoutManager(
                new GridLayoutManager(getContext(), 2));
        productAdapter = new ProductAdapter(getContext(), productList);
        recyclerProducts.setAdapter(productAdapter);

        productAdapter.setOnProductClickListener(product ->
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container,
                                ProductDetailFragment.newInstance(
                                        product.getId(),
                                        product.getCategoryId()))
                        .addToBackStack(null)
                        .commit());

        loadCategories();
        return view;
    }

    // ── Load all categories from products/{adminUid} ──────────
    private void loadCategories() {
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot adminSnap) {
                categoryList.clear();

                // Add an "All" sentinel category first
                categoryList.add(new Category("all", "All", ""));

                for (DataSnapshot catSnap : adminSnap.getChildren()) {
                    String catId   = catSnap.getKey();
                    String catName = catSnap.child("name")
                            .getValue(String.class);
                    String catImg  = catSnap.child("imageURL")
                            .getValue(String.class);

                    if (catName != null) {
                        categoryList.add(
                                new Category(catId, catName,
                                        catImg != null ? catImg : ""));
                    }
                }

                categoryAdapter.notifyDataSetChanged();
                categoryAdapter.setSelected("all");
                // Load all products initially
                loadProductsByCategory("all");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),
                        "Error loading categories",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Load products filtered by categoryId ──────────────────
    private void loadProductsByCategory(String categoryId) {
        progressBar.setVisibility(View.VISIBLE);

        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot adminSnap) {
                productList.clear();

                for (DataSnapshot catSnap : adminSnap.getChildren()) {
                    String catId   = catSnap.getKey();
                    String catName = catSnap.child("name")
                            .getValue(String.class);

                    // Skip categories that don't match the filter
                    if (!categoryId.equals("all")
                            && !categoryId.equals(catId)) continue;

                    DataSnapshot itemsSnap = catSnap.child("items");
                    for (DataSnapshot productSnap : itemsSnap.getChildren()) {
                        Product p = productSnap.getValue(Product.class);
                        if (p != null) {
                            p.setId(productSnap.getKey());
                            p.setCategoryId(catId);
                            p.setCategoryName(catName);
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
                Toast.makeText(getContext(),
                        "Error loading products",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}