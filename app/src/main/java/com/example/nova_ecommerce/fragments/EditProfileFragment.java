package com.example.nova_ecommerce.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.nova_ecommerce.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private ImageView imgEditAvatar;
    private TextInputEditText etName, etEmail, etPhone, etAddress, etPassword;
    private Button btnSave;
    private ImageView btnBack;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private FirebaseUser currentUser;
    private String userId;

    private Uri selectedImageUri;
    private String currentName, currentEmail, currentPhone, currentAddress, currentImageUrl;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).circleCrop().into(imgEditAvatar);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance("https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com").getReference("users").child(userId);
        }

        imgEditAvatar = view.findViewById(R.id.imgEditAvatar);
        etName = view.findViewById(R.id.etEditName);
        etEmail = view.findViewById(R.id.etEditEmail);
        etPhone = view.findViewById(R.id.etEditPhone);
        etAddress = view.findViewById(R.id.etEditAddress);
        etPassword = view.findViewById(R.id.etEditPassword);
        btnSave = view.findViewById(R.id.btnSaveProfile);
        btnBack = view.findViewById(R.id.btnBack);

        loadUserData();

        view.findViewById(R.id.btnChangeAvatar).setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnSave.setOnClickListener(v -> validateAndSave());

        return view;
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentName = snapshot.child("name").getValue(String.class);
                    currentEmail = snapshot.child("email").getValue(String.class);
                    currentPhone = snapshot.child("phone").getValue(String.class);
                    currentAddress = snapshot.child("address").getValue(String.class);
                    currentImageUrl = snapshot.child("profileImage").getValue(String.class);

                    etName.setText(currentName);
                    etEmail.setText(currentEmail);
                    etPhone.setText(currentPhone);
                    etAddress.setText(currentAddress);

                    if (currentImageUrl != null && !currentImageUrl.isEmpty() && !currentImageUrl.contains("placeholder")) {
                        Glide.with(requireContext()).load(currentImageUrl).circleCrop().placeholder(R.drawable.ic_person).into(imgEditAvatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void validateAndSave() {
        String newName = etName.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();
        String newAddress = etAddress.getText().toString().trim();
        String newPassword = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newName)) {
            Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean emailChanged = !newEmail.equals(currentEmail);
        boolean passwordChanged = !TextUtils.isEmpty(newPassword);
        boolean otherChanged = !newName.equals(currentName) || !newPhone.equals(currentPhone) || !newAddress.equals(currentAddress) || selectedImageUri != null;

        if (!emailChanged && !passwordChanged && !otherChanged) {
            Toast.makeText(getContext(), "No changes detected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (emailChanged || passwordChanged) {
            // Re-authentication might be required for email/password change
            // For simplicity in this ecommerce app, we'll try directly or ask for a simple flow
            // But usually, verifyBeforeUpdateEmail is better.
            updateAuthAndData(newEmail, newPassword, newName, newPhone, newAddress, emailChanged, passwordChanged);
        } else {
            saveOtherData(newName, newPhone, newAddress);
        }
    }

    private void updateAuthAndData(String email, String password, String name, String phone, String address, boolean emailChanged, boolean passwordChanged) {
        if (emailChanged) {
            currentUser.verifyBeforeUpdateEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Verification email sent to new address. Please verify to complete email change.", Toast.LENGTH_LONG).show();
                    // We can still update other data
                    saveOtherData(name, phone, address);
                } else {
                    Toast.makeText(getContext(), "Email update failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (passwordChanged) {
            currentUser.updatePassword(password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Password update failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (!emailChanged) {
            saveOtherData(name, phone, address);
        }
    }

    private void saveOtherData(String name, String phone, String address) {
        if (selectedImageUri != null) {
            uploadImageAndSaveData(name, phone, address);
        } else {
            updateDatabase(name, phone, address, currentImageUrl);
        }
    }

    private void uploadImageAndSaveData(String name, String phone, String address) {
        Toast.makeText(getContext(), "Uploading image...", Toast.LENGTH_SHORT).show();
        StorageReference ref = FirebaseStorage.getInstance().getReference("profile_images/" + userId + ".jpg");
        ref.putFile(selectedImageUri).addOnSuccessListener(task -> ref.getDownloadUrl().addOnSuccessListener(url -> {
            updateDatabase(name, phone, address, url.toString());
        })).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            updateDatabase(name, phone, address, currentImageUrl);
        });
    }

    private void updateDatabase(String name, String phone, String address, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("address", address);
        updates.put("profileImage", imageUrl);

        userRef.updateChildren(updates).addOnSuccessListener(unused -> {
            Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to update database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
