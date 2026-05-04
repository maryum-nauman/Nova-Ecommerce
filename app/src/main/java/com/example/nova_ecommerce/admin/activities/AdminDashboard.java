package com.example.nova_ecommerce.admin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.user.activities.Login;
import com.example.nova_ecommerce.admin.fragments.AdminCategoriesFragment;
import com.example.nova_ecommerce.admin.fragments.AdminInboxFragment;
import com.example.nova_ecommerce.admin.fragments.AdminOrdersFragment;
import com.example.nova_ecommerce.admin.fragments.AdminProductsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboard extends AppCompatActivity {

    private AdminCategoriesFragment categoriesFragment;
    private EditText                etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        ImageButton btnLogout = findViewById(R.id.btnAdminLogout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            getSharedPreferences("NovaPrefs", MODE_PRIVATE)
                    .edit().clear().apply();
            Intent intent = new Intent(AdminDashboard.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Fragment current = getSupportFragmentManager()
                        .findFragmentById(R.id.admin_fragment_container);
                if (current instanceof AdminCategoriesFragment) {
                    ((AdminCategoriesFragment) current)
                            .filterProducts(s.toString().trim());
                } else if (current instanceof AdminProductsFragment) {
                    ((AdminProductsFragment) current)
                            .filterProducts(s.toString().trim());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        BottomNavigationView bottomNav =
                findViewById(R.id.admin_bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.admin_nav_categories) {
                if (categoriesFragment == null)
                    categoriesFragment = new AdminCategoriesFragment();
                selectedFragment = categoriesFragment;
                showSearch();
            } else if (id == R.id.admin_nav_products) {
                selectedFragment = new AdminProductsFragment();
                showSearch();
            } else if (id == R.id.admin_nav_orders) {
                selectedFragment = new AdminOrdersFragment();
                hideSearch();
            }else if (id == R.id.admin_nav_inbox) {
                selectedFragment = new AdminInboxFragment();
                hideSearch();
            }
            if (selectedFragment != null) loadFragment(selectedFragment);
            return true;
        });

        categoriesFragment = new AdminCategoriesFragment();
        loadFragment(categoriesFragment);
        bottomNav.setSelectedItemId(R.id.admin_nav_categories);
        showSearch();
    }

    private void showSearch() {
        etSearch.setVisibility(View.VISIBLE);
        etSearch.setText("");
    }

    private void hideSearch() {
        etSearch.setText("");
        etSearch.setVisibility(View.GONE);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.admin_fragment_container, fragment)
                .commit();
    }
}