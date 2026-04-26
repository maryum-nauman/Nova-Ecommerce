package com.example.nova_ecommerce.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nova_ecommerce.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private TextView tvUserName, tvUserEmail, tvOrderCount, tvFavCount, tvCartCount;
    private FirebaseFirestore db;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvOrderCount = view.findViewById(R.id.tvOrderCount);
        tvFavCount = view.findViewById(R.id.tvFavCount);
        tvCartCount = view.findViewById(R.id.tvCartCount);

        db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            // Show email, use part before @ as display name
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
        // Favorites count
        db.collection("users").document(userId)
                .collection("favorites")
                .get()
                .addOnSuccessListener(snap ->
                        tvFavCount.setText(String.valueOf(snap.size())));

        // Cart count
        db.collection("users").document(userId)
                .collection("cart")
                .get()
                .addOnSuccessListener(snap ->
                        tvCartCount.setText(String.valueOf(snap.size())));
    }
}