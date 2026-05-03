package com.example.nova_ecommerce.admin.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.admin.adapters.AdminCategoryAdapter;
import com.example.nova_ecommerce.models.Category;
import com.example.nova_ecommerce.models.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminCategoriesFragment extends Fragment {

    private static final String ADMIN_UID = "48ULkpPhYfVOAAfqcKbD7VtXOyt1";

    private RecyclerView              recyclerCategories;
    private AdminCategoryAdapter      adapter;
    private ProgressBar               progressBar;
    private TextView                  tvEmpty;
    private FloatingActionButton      fabAddCategory;
    private DatabaseReference         adminRef;

    private final List<Category> categoryList = new ArrayList<>();
    private final List<Category> searchList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_admin_categories, container, false);

        recyclerCategories = view.findViewById(R.id.recyclerAdminCategories);
        progressBar        = view.findViewById(R.id.progressBarAdminCat);
        tvEmpty            = view.findViewById(R.id.tvEmptyAdminCat);
        fabAddCategory     = view.findViewById(R.id.fabAddCategory);

        adminRef = FirebaseDatabase.getInstance("https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com"
        ).getReference("products").child(ADMIN_UID);

        recyclerCategories.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new AdminCategoryAdapter(getContext(), searchList, category -> showEditCategoryDialog(category),
                category -> showDeleteCategoryDialog(category)
        );
        recyclerCategories.setAdapter(adapter);
        fabAddCategory.setOnClickListener(v -> showAddCategoryDialog());
        loadCategories();
        return view;
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);
        adminRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot catSnap : snapshot.getChildren()) {
                    String catId   = catSnap.getKey();
                    String catName = catSnap.child("name").getValue(String.class);
                    String catImg  = catSnap.child("imageURL").getValue(String.class);
                    if (catName != null) {
                        categoryList.add(new Category(catId, catName, catImg != null ? catImg : ""));
                    }
                }
                searchList.clear();
                searchList.addAll(categoryList);
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(categoryList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddCategoryDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_category, null);
        EditText etName = view.findViewById(R.id.etCatName);
        EditText etImageUrl = view.findViewById(R.id.etCatImageUrl);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog_Nova)
                .setTitle("Create New Category")
                .setIcon(R.drawable.ic_categories) //
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String img = etImageUrl.getText().toString().trim();
                    if (!name.isEmpty()) {
                        String id = adminRef.push().getKey();
                        adminRef.child(id).child("name").setValue(name);
                        adminRef.child(id).child("imageURL").setValue(img.isEmpty() ? "" : img);
                        Toast.makeText(getContext(), "Category added successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditCategoryDialog(Category category) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_category, null);
        EditText etName = view.findViewById(R.id.etCatName);
        EditText etImageUrl = view.findViewById(R.id.etCatImageUrl);

        etName.setText(category.getName());
        etImageUrl.setText(category.getImageURL());

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog_Nova)
                .setTitle("Update Category")
                .setIcon(android.R.drawable.ic_menu_edit)
                .setView(view)
                .setPositiveButton("Save Changes", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String img = etImageUrl.getText().toString().trim();
                    if (!name.isEmpty()) {
                        adminRef.child(category.getId()).child("name").setValue(name);
                        adminRef.child(category.getId()).child("imageURL").setValue(img);
                        Toast.makeText(getContext(), "Updated!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteCategoryDialog(Category category) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog_Nova)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete \"" + category.getName() + "\"? This will remove all products inside it.")
                .setIcon(R.drawable.ic_delete) //
                .setPositiveButton("Delete", (dialog, which) -> {
                    adminRef.child(category.getId()).removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Category removed", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Keep It", null)
                .show();
    }
    public void filterProducts(String query) {
        if (adapter == null || categoryList.isEmpty()) return;
        searchList.clear();

        if (query.isEmpty()) {
            searchList.addAll(categoryList);
            tvEmpty.setVisibility(View.GONE);
        } else {
            String lower = query.toLowerCase();
            for (Category p : categoryList) {
                if ((p.getName() != null && p.getName().toLowerCase().contains(lower)) || (p.getName() != null && p.getName().toLowerCase().contains(lower))) {
                    searchList.add(p);
                }
            }
            tvEmpty.setVisibility(
                    searchList.isEmpty() ? View.VISIBLE : View.GONE);
        }
        adapter.notifyDataSetChanged();
    }
}