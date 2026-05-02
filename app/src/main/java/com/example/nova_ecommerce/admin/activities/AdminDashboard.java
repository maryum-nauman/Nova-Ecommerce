package com.example.nova_ecommerce.admin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.activities.Login;
import com.example.nova_ecommerce.admin.fragments.AdminCategoriesFragment;
import com.example.nova_ecommerce.admin.fragments.AdminOrdersFragment;
import com.example.nova_ecommerce.admin.fragments.AdminProductsFragment;
import com.example.nova_ecommerce.fragments.ShopFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboard extends AppCompatActivity {

    private AdminCategoriesFragment categoriesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // ── Logout ────────────────────────────────────────────────
        ImageButton btnLogout = findViewById(R.id.btnAdminLogout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            getSharedPreferences("NovaPrefs", MODE_PRIVATE)
                    .edit().clear().apply();
            Intent intent = new Intent(AdminDashboard.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        // ── Search ────────────────────────────────────────────────
        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s,
                                      int start, int before, int count) {
                Fragment current = getSupportFragmentManager()
                        .findFragmentById(R.id.admin_fragment_container);
                if (current instanceof AdminCategoriesFragment) {
                    ((AdminCategoriesFragment) current)
                            .filterProducts(s.toString().trim());
                }
                if (current instanceof AdminProductsFragment) {
                    ((AdminProductsFragment) current)
                            .filterProducts(s.toString().trim());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // ── Bottom Navigation ─────────────────────────────────────
        BottomNavigationView bottomNav = findViewById(R.id.admin_bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.admin_nav_categories) {
                if (categoriesFragment == null)
                    categoriesFragment = new AdminCategoriesFragment();
                selectedFragment = categoriesFragment;
            } else if (id == R.id.admin_nav_products) {
                selectedFragment = new AdminProductsFragment();
            } else if (id == R.id.admin_nav_orders) {
                selectedFragment = new AdminOrdersFragment();
            }

            if (selectedFragment != null) loadFragment(selectedFragment);
            return true;
        });

        // ── Default: open Categories tab ──────────────────────────
        categoriesFragment = new AdminCategoriesFragment();
        loadFragment(categoriesFragment);
        bottomNav.setSelectedItemId(R.id.admin_nav_categories);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.admin_fragment_container, fragment)
                .commit();
    }
}