package com.example.nova_ecommerce.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.database.CartDatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {

    private static final String DB_URL =
            "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com";

    // ── Views ─────────────────────────────────────────────────
    private ImageView imgAvatar;
    private TextView  tvUserName, tvUserEmail;
    private TextView  tvOrderCount, tvFavCount, tvCartCount;
    private TextView  tvPendingCount, tvShippedCount, tvDeliveredCount;

    // ── State ─────────────────────────────────────────────────
    private String userId;
    private DatabaseReference userRef;

    // ── Image picker launcher ─────────────────────────────────
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {
                            Uri imageUri = result.getData().getData();
                            if (imageUri != null) uploadProfileImage(imageUri);
                        }
                    });

    // ─────────────────────────────────────────────────────────
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_profile, container, false);

        // Bind views
        imgAvatar       = view.findViewById(R.id.imgAvatar);
        tvUserName      = view.findViewById(R.id.tvUserName);
        tvUserEmail     = view.findViewById(R.id.tvUserEmail);
        tvOrderCount    = view.findViewById(R.id.tvOrderCount);
        tvFavCount      = view.findViewById(R.id.tvFavCount);
        tvCartCount     = view.findViewById(R.id.tvCartCount);
        tvPendingCount  = view.findViewById(R.id.tvPendingCount);
        tvShippedCount  = view.findViewById(R.id.tvShippedCount);
        tvDeliveredCount= view.findViewById(R.id.tvDeliveredCount);

        // Navigation click listeners
        View.OnClickListener goToOrders = v -> navigateTo(new OrdersFragment());
        view.findViewById(R.id.tvViewAllOrders).setOnClickListener(goToOrders);
        view.findViewById(R.id.layoutStatOrders).setOnClickListener(goToOrders);
        view.findViewById(R.id.layoutStatFavs).setOnClickListener(v -> navigateTo(new FavoritesFragment()));
        view.findViewById(R.id.layoutStatCart).setOnClickListener(v -> navigateTo(new CartFragment()));

        // Status filter shortcuts (also go to orders for now)
        view.findViewById(R.id.layoutPending).setOnClickListener(goToOrders);
        view.findViewById(R.id.layoutShipped).setOnClickListener(goToOrders);
        view.findViewById(R.id.layoutDelivered).setOnClickListener(goToOrders);

        // Edit avatar tap
        view.findViewById(R.id.btnEditAvatar).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId  = user.getUid();
            userRef = FirebaseDatabase.getInstance(DB_URL)
                    .getReference("users").child(userId);

            tvUserEmail.setText(user.getEmail());
            loadProfile();
            loadCounts();
            loadOrderStatusCounts();
        }

        return view;
    }

    private void navigateTo(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    // ── Load name + profileImage from Realtime DB ─────────────
    private void loadProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Name — use DB name, fall back to email prefix
                String name = snapshot.child("name").getValue(String.class);
                if (name != null && !name.isEmpty()) {
                    tvUserName.setText(name);
                } else {
                    FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
                    if (u != null && u.getEmail() != null) {
                        tvUserName.setText(u.getEmail().split("@")[0]);
                    }
                }

                // Profile image
                String imageUrl = snapshot.child("profileImage")
                        .getValue(String.class);
                if (imageUrl != null && !imageUrl.isEmpty()
                        && !imageUrl.contains("placeholder")) {
                    Glide.with(requireContext())
                            .load(imageUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_person)
                            .into(imgAvatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ── Load stats counts ─────────────────────────────────────
    private void loadCounts() {
        if (userId == null) return;

        // Favorites — Firebase Realtime DB
        userRef.child("favorites")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        tvFavCount.setText(
                                String.valueOf(snapshot.getChildrenCount()));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                        tvFavCount.setText("0");
                    }
                });

        // Orders total — Firebase Realtime DB
        userRef.child("orders")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        tvOrderCount.setText(
                                String.valueOf(snapshot.getChildrenCount()));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                        tvOrderCount.setText("0");
                    }
                });

        // Cart — local SQLite
        int cartCount = CartDatabaseHelper
                .getInstance(requireContext())
                .getAllItems(userId)
                .size();
        tvCartCount.setText(String.valueOf(cartCount));
    }

    // ── Load per-status order counts ──────────────────────────
    private void loadOrderStatusCounts() {
        userRef.child("orders")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int pending = 0, shipped = 0, delivered = 0;

                        for (DataSnapshot order : snapshot.getChildren()) {
                            String status = order.child("status")
                                    .getValue(String.class);
                            if (status == null) continue;
                            switch (status.toLowerCase()) {
                                case "pending":   pending++;   break;
                                case "shipped":   shipped++;   break;
                                case "delivered": delivered++; break;
                            }
                        }

                        setBadge(tvPendingCount,   pending);
                        setBadge(tvShippedCount,   shipped);
                        setBadge(tvDeliveredCount, delivered);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    /** Shows badge with count; hides it when count is 0. */
    private void setBadge(TextView badge, int count) {
        if (count > 0) {
            badge.setText(String.valueOf(count));
            badge.setVisibility(View.VISIBLE);
        } else {
            badge.setVisibility(View.GONE);
        }
    }

    // ── Upload profile image to Firebase Storage ──────────────
    private void uploadProfileImage(Uri imageUri) {
        Toast.makeText(getContext(),
                "Uploading photo…", Toast.LENGTH_SHORT).show();

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("profile_images/" + userId + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl()
                                .addOnSuccessListener(downloadUri -> {
                                    String url = downloadUri.toString();

                                    // 1. Update DB
                                    userRef.child("profileImage").setValue(url);

                                    // 2. Update UI immediately
                                    Glide.with(requireContext())
                                            .load(url)
                                            .circleCrop()
                                            .into(imgAvatar);

                                    Toast.makeText(getContext(),
                                            "Profile photo updated!",
                                            Toast.LENGTH_SHORT).show();
                                }))
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Upload failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}
