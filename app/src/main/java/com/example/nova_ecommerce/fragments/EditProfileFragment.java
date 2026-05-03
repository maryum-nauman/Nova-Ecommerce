package com.example.nova_ecommerce.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.nova_ecommerce.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    private String currentName, currentEmail, currentPhone, currentAddress, currentImageUrl;

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

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnSave.setOnClickListener(v -> validateAndSave());

        return view;
    }

    private void loadUserData() {
        if (userRef == null) return;
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

                    if (!TextUtils.isEmpty(currentImageUrl) && !currentImageUrl.contains("placeholder")) {
                        if (isAdded()) {
                            Glide.with(requireContext()).load(currentImageUrl).circleCrop().placeholder(R.drawable.ic_person).into(imgEditAvatar);
                        }
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
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

        boolean emailChanged = !newEmail.equalsIgnoreCase(currentEmail);
        boolean passwordChanged = !TextUtils.isEmpty(newPassword);
        boolean phoneChanged = !TextUtils.equals(newPhone, currentPhone);
        boolean infoChanged = !newName.equals(currentName) || !TextUtils.equals(newAddress, currentAddress);

        if (!emailChanged && !passwordChanged && !phoneChanged && !infoChanged) {
            Toast.makeText(getContext(), "No changes detected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phoneChanged) {
            showOtpDialog(() -> {
                if (emailChanged || passwordChanged) {
                    updateAuthAndData(newEmail, newPassword, newName, newPhone, newAddress, emailChanged, passwordChanged);
                } else {
                    updateDatabase(newName, newPhone, newAddress, currentEmail);
                }
            });
        } else if (emailChanged || passwordChanged) {
            updateAuthAndData(newEmail, newPassword, newName, newPhone, newAddress, emailChanged, passwordChanged);
        } else {
            updateDatabase(newName, newPhone, newAddress, currentEmail);
        }
    }

    private void showOtpDialog(Runnable onSuccess) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_otp, null);
        EditText etOtp = dialogView.findViewById(R.id.etOtp);
        Button btnVerify = dialogView.findViewById(R.id.btnVerifyOtp);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnVerify.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();
            if ("123456".equals(otp)) { // Simulated OTP
                dialog.dismiss();
                onSuccess.run();
            } else {
                Toast.makeText(getContext(), "Invalid OTP", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void updateAuthAndData(String email, String password, String name, String phone, String address, boolean emailChanged, boolean passwordChanged) {
        if (emailChanged) {
            currentUser.verifyBeforeUpdateEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Verification email sent to " + email + ". Please verify to complete change.", Toast.LENGTH_LONG).show();
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

        // Save other changes to DB immediately (keeping old email in DB until verification is complete)
        updateDatabase(name, phone, address, emailChanged ? currentEmail : email);
    }

    private void updateDatabase(String name, String phone, String address, String email) {
        if (userRef == null) return;
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("address", address);
        updates.put("email", email);

        userRef.updateChildren(updates).addOnSuccessListener(unused -> {
            if (isAdded()) {
                Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            }
        }).addOnFailureListener(e -> {
            if (isAdded()) Toast.makeText(getContext(), "Failed to update database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
