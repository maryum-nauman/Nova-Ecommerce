package com.example.nova_ecommerce.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.fragments.CartFragment;
import com.example.nova_ecommerce.fragments.CategoryFragment;
import com.example.nova_ecommerce.fragments.FavoritesFragment;
import com.example.nova_ecommerce.fragments.ProfileFragment;
import com.example.nova_ecommerce.fragments.ShopFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class Dashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        ImageButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // 1. Sign out from Firebase Authentication
            FirebaseAuth.getInstance().signOut();

            // 2. Clear Local Session Management (SharedPreferences)
            SharedPreferences sharedPreferences = getSharedPreferences("NovaPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear(); // This removes "Remember Me" data
            editor.apply();

            // 3. Navigate back to Login Screen
            Intent intent = new Intent(Dashboard.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_shop) selectedFragment = new ShopFragment();
            else if (id == R.id.nav_category) selectedFragment = new CategoryFragment();
            else if (id == R.id.nav_cart) selectedFragment = new CartFragment();
            else if (id == R.id.nav_fav) selectedFragment = new FavoritesFragment();
            else if (id == R.id.nav_profile) selectedFragment = new ProfileFragment();

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });

        // Set default fragment
        loadFragment(new ShopFragment());
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
