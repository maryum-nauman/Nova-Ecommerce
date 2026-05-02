package com.example.nova_ecommerce.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.database.CartDatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TextView tvUserName, tvUserEmail, tvOrderCount, tvFavCount, tvCartCount;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvUserName   = view.findViewById(R.id.tvUserName);
        tvUserEmail  = view.findViewById(R.id.tvUserEmail);
        tvOrderCount = view.findViewById(R.id.tvOrderCount);
        tvFavCount   = view.findViewById(R.id.tvFavCount);
        tvCartCount  = view.findViewById(R.id.tvCartCount);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();

            // Display name / email
            tvUserEmail.setText(user.getEmail());
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                tvUserName.setText(user.getDisplayName());
            } else if (user.getEmail() != null) {
                tvUserName.setText(user.getEmail().split("@")[0]);
            }

            loadCounts();
        }

        return view;
    }

    private void loadCounts() {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        // ── 1. FAVORITES — read from Firebase Realtime DB ──────────────
        userRef.child("favorites")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        tvFavCount.setText(String.valueOf(snapshot.getChildrenCount()));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvFavCount.setText("0");
                    }
                });

        // ── 2. ORDERS — read from Firebase Realtime DB ─────────────────
        userRef.child("orders")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        tvOrderCount.setText(String.valueOf(snapshot.getChildrenCount()));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvOrderCount.setText("0");
                    }
                });

        // ── 3. CART — read from LOCAL SQLite ───────────────────────────
        int cartCount = CartDatabaseHelper.getInstance(requireContext()).getAllItems(userId).size();
        tvCartCount.setText(String.valueOf(cartCount));
    }
}