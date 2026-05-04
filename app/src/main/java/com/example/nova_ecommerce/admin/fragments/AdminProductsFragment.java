package com.example.nova_ecommerce.admin.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.admin.adapters.AdminProductAdapter;
import com.example.nova_ecommerce.user.models.Category;
import com.example.nova_ecommerce.user.models.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminProductsFragment extends Fragment {

    private static final String ADMIN_UID = "48ULkpPhYfVOAAfqcKbD7VtXOyt1";
    private RecyclerView         recyclerProducts;
    private AdminProductAdapter adapter;
    private ProgressBar          progressBar;
    private TextView             tvEmpty;
    private FloatingActionButton fabAddProduct;
    private DatabaseReference    adminRef;

    private final List<Product>  productList  = new ArrayList<>();

    private final List<Product>  searchList  = new ArrayList<>();

    private final List<Category> categoryList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_products, container, false);

        recyclerProducts = view.findViewById(R.id.recyclerAdminProducts);
        progressBar      = view.findViewById(R.id.progressBarAdminProd);
        tvEmpty          = view.findViewById(R.id.tvEmptyAdminProd);
        fabAddProduct    = view.findViewById(R.id.fabAddProduct);

        adminRef = FirebaseDatabase.getInstance("https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com").getReference("products").child(ADMIN_UID);
        recyclerProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        // ── Replace adapter initialization in onCreateView ────────
        adapter = new AdminProductAdapter(
                getContext(),
                searchList,
                product -> showProductDialog(true, product),
                product -> showDeleteProductDialog(product),
                (product, newFeaturedState) ->
                        updateFeaturedInFirebase(product, newFeaturedState)
        );
        recyclerProducts.setAdapter(adapter);

        fabAddProduct.setOnClickListener(v -> showProductDialog(false, null));

        loadAllProducts();
        return view;
    }
    private void updateFeaturedInFirebase(Product product,
                                          boolean isFeatured) {
        adminRef.child(product.getCategoryId())
                .child("items")
                .child(product.getId())
                .child("isFeatured")
                .setValue(isFeatured)
                .addOnSuccessListener(unused -> {
                    String msg = isFeatured
                            ? "🔥 Added to Hot Deals!"
                            : "Removed from Hot Deals";
                    Toast.makeText(getContext(), msg,
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
    private void loadAllProducts() {
        progressBar.setVisibility(View.VISIBLE);
        adminRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot adminSnap) {
                productList.clear();
                categoryList.clear();

                for (DataSnapshot catSnap : adminSnap.getChildren()) {
                    String catId   = catSnap.getKey();
                    String catName = catSnap.child("name").getValue(String.class);
                    String catImg  = catSnap.child("imageURL").getValue(String.class);

                    if (catName != null) {
                        categoryList.add(new Category(catId, catName, catImg != null ? catImg : ""));
                    }

                    for (DataSnapshot productSnap : catSnap.child("items").getChildren()) {
                        Product p = productSnap.getValue(Product.class);
                        if (p != null) {
                            p.setId(productSnap.getKey());
                            p.setCategoryId(catId);
                            p.setCategoryName(catName);
                            productList.add(p);
                        }
                    }
                }
                searchList.clear();
                searchList.addAll(productList);

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(productList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProductDialog(boolean isEdit, @Nullable Product existing) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_product, null);

        EditText etName = dialogView.findViewById(R.id.etProdName);
        EditText etDescription = dialogView.findViewById(R.id.etProdDescription);
        EditText etPrice = dialogView.findViewById(R.id.etProdPrice);
        EditText etImageUrl = dialogView.findViewById(R.id.etProdImageUrl);
        EditText etStock = dialogView.findViewById(R.id.etProdStock);
        CheckBox cbFeatured = dialogView.findViewById(R.id.cbProdFeatured);
        Spinner spinnerCat = dialogView.findViewById(R.id.spinnerCategory);

        List<String> catNames = new ArrayList<>();
        for (Category c : categoryList) catNames.add(c.getName());
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, catNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(spinnerAdapter);

        if (isEdit && existing != null) {
            etName.setText(existing.getName());
            etDescription.setText(existing.getDescription());
            etPrice.setText(String.valueOf(existing.getPrice()));
            etImageUrl.setText(existing.getImageURL());
            etStock.setText(String.valueOf(existing.getStock()));
            cbFeatured.setChecked(existing.isFeatured());

            for (int i = 0; i < categoryList.size(); i++) {
                if (categoryList.get(i).getId().equals(existing.getCategoryId())) {
                    spinnerCat.setSelection(i);
                    break;
                }
            }
        }

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog_Nova)
                .setTitle(isEdit ? "Update Product" : "Add New Product")
                .setIcon(isEdit ? android.R.drawable.ic_menu_edit : R.drawable.ic_products)
                .setView(dialogView)
                .setPositiveButton(isEdit ? "Save" : "Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String desc = etDescription.getText().toString().trim();
                    String priceS = etPrice.getText().toString().trim();
                    String imgUrl = etImageUrl.getText().toString().trim();
                    String stockS = etStock.getText().toString().trim();
                    boolean feat = cbFeatured.isChecked();

                    if (name.isEmpty() || priceS.isEmpty()) {
                        Toast.makeText(getContext(), "Name and price are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double price = Double.parseDouble(priceS);
                    int stock = stockS.isEmpty() ? 0 : Integer.parseInt(stockS);

                    int selectedPos = spinnerCat.getSelectedItemPosition();
                    if (selectedPos < 0 || selectedPos >= categoryList.size()) {
                        Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Category selectedCat = categoryList.get(selectedPos);

                    Map<String, Object> productMap = new HashMap<>();
                    productMap.put("name", name);
                    productMap.put("description", desc);
                    productMap.put("price", price);
                    productMap.put("imageURL", imgUrl.isEmpty() ? "https://via.placeholder.com/300" : imgUrl);
                    productMap.put("stock", stock);
                    productMap.put("isFeatured", feat);
                    productMap.put("rating", isEdit ? existing.getRating() : 0.0);
                    productMap.put("reviewCount", isEdit ? existing.getReviewCount() : 0);

                    if (isEdit && existing != null) {
                        boolean catChanged = !existing.getCategoryId().equals(selectedCat.getId());

                        if (catChanged) {
                            adminRef.child(existing.getCategoryId()).child("items").child(existing.getId()).removeValue();

                            String newProdId = adminRef.child(selectedCat.getId()).child("items").push().getKey();
                            adminRef.child(selectedCat.getId()).child("items").child(newProdId).setValue(productMap)
                                    .addOnSuccessListener(u -> Toast.makeText(getContext(), "Product moved & updated!", Toast.LENGTH_SHORT).show());
                        } else {
                            adminRef.child(selectedCat.getId()).child("items").child(existing.getId()).updateChildren(productMap)
                                    .addOnSuccessListener(u -> Toast.makeText(getContext(), "Product updated!", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        String newProdId = adminRef.child(selectedCat.getId()).child("items").push().getKey();
                        adminRef.child(selectedCat.getId()).child("items").child(newProdId).setValue(productMap)
                                .addOnSuccessListener(u -> Toast.makeText(getContext(), "Product added!", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteProductDialog(Product product) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog_Nova)
                .setTitle("Remove Product")
                .setMessage("Are you sure you want to delete \"" + product.getName() + "\"?")
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton("Delete", (dialog, which) -> {
                    adminRef.child(product.getCategoryId())
                            .child("items")
                            .child(product.getId())
                            .removeValue()
                            .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Product deleted", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    public void filterProducts(String query) {
        if (adapter == null || productList.isEmpty()) return;
        searchList.clear();

        if (query.isEmpty()) {
            searchList.addAll(productList);
            tvEmpty.setVisibility(View.GONE);
        } else {
            String lower = query.toLowerCase();
            for (Product p : productList) {
                if ((p.getName() != null && p.getName().toLowerCase().contains(lower)) || (p.getCategoryName() != null && p.getCategoryName().toLowerCase().contains(lower)) || (p.getDescription() != null && p.getDescription().toLowerCase().contains(lower))) {
                    searchList.add(p);
                }
            }
            tvEmpty.setVisibility(searchList.isEmpty() ? View.VISIBLE : View.GONE);
        }
        adapter.notifyDataSetChanged();
    }
}