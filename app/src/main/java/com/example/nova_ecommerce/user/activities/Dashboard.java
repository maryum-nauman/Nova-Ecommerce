package com.example.nova_ecommerce.user.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.user.fragments.CartFragment;
import com.example.nova_ecommerce.user.fragments.CategoryFragment;
import com.example.nova_ecommerce.user.fragments.FavoritesFragment;
import com.example.nova_ecommerce.user.fragments.ProfileFragment;
import com.example.nova_ecommerce.user.fragments.ShopFragment;
import com.example.nova_ecommerce.user.fragments.UserChatFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class Dashboard extends AppCompatActivity {

    private ShopFragment shopFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        ImageButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            getSharedPreferences("NovaPrefs", MODE_PRIVATE)
                    .edit().clear().apply();
            Intent intent = new Intent(Dashboard.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Only filter when ShopFragment is active
                Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (current instanceof ShopFragment) {
                    ((ShopFragment) current).filterProducts(s.toString().trim());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            etSearch.setText("");

            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_shop) {
                if (shopFragment == null) shopFragment = new ShopFragment();
                selectedFragment = shopFragment;
            } else if (id == R.id.nav_category) {
                selectedFragment = new CategoryFragment();
            } else if (id == R.id.nav_cart) {
                selectedFragment = new CartFragment();
            } else if (id == R.id.nav_fav) {
                selectedFragment = new FavoritesFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else if (id == R.id.nav_chat) {
                selectedFragment = new UserChatFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });

        shopFragment = new ShopFragment();
        loadFragment(shopFragment);
        bottomNav.setSelectedItemId(R.id.nav_shop);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}