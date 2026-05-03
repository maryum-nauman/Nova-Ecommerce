package com.example.nova_ecommerce.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.activities.Login;
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

import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private ImageView imgEditAvatar;
    private TextInputEditText etName, etEmail, etPhone, etAddress, etPassword;
    private Button btnSave;
    private ImageView btnBack;
    private TextView tvDeleteAccount, tvLogout;

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
        tvDeleteAccount = view.findViewById(R.id.tvDeleteAccount);
        tvLogout = view.findViewById(R.id.tvLogout);

        loadUserData();

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnSave.setOnClickListener(v -> validateAndSave());
        tvDeleteAccount.setOnClickListener(v -> showDeleteConfirmation());
        tvLogout.setOnClickListener(v -> logoutAndRedirect());

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
        boolean phoneChanged = !TextUtils.equals(newPhone, currentPhone);
        boolean passwordChanged = !TextUtils.isEmpty(newPassword);
        boolean infoChanged = !newName.equals(currentName) || !TextUtils.equals(newAddress, currentAddress);

        if (!emailChanged && !phoneChanged && !passwordChanged && !infoChanged) {
            Toast.makeText(getContext(), "No changes detected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (emailChanged && !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            Toast.makeText(getContext(), "Invalid Email Address", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phoneChanged && newPhone.length() < 10) {
            Toast.makeText(getContext(), "Invalid Phone Number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (emailChanged || phoneChanged || passwordChanged) {
            String target = emailChanged ? newEmail : (phoneChanged ? newPhone : currentEmail);
            Toast.makeText(getContext(), "OTP SENT TO: " + target + "\nCODE: 123456", Toast.LENGTH_LONG).show();
            showOtpDialog(() -> promptForPasswordToUpdate(newEmail, newPassword, newName, newPhone, newAddress, emailChanged, passwordChanged));
        } else {
            updateDatabase(newName, newPhone, newAddress, currentEmail, false);
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
            if ("123456".equals(otp)) {
                dialog.dismiss();
                onSuccess.run();
            } else {
                Toast.makeText(getContext(), "Invalid OTP. Use: 123456", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void promptForPasswordToUpdate(String email, String password, String name, String phone, String address, boolean emailChanged, boolean passwordChanged) {
        EditText etPass = new EditText(getContext());
        etPass.setHint("Enter current password to verify");
        etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        int p = (int) (24 * getResources().getDisplayMetrics().density);
        etPass.setPadding(p, p, p, p);

        new AlertDialog.Builder(requireContext())
                .setTitle("Verify Identity")
                .setMessage("Enter your current password to save sensitive changes.")
                .setView(etPass)
                .setPositiveButton("Verify", (dialog, which) -> {
                    String curPass = etPass.getText().toString().trim();
                    if (!TextUtils.isEmpty(curPass)) {
                        reauthenticateAndSave(curPass, email, password, name, phone, address, emailChanged, passwordChanged);
                    } else {
                        Toast.makeText(getContext(), "Password required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reauthenticateAndSave(String curPass, String email, String password, String name, String phone, String address, boolean emailChanged, boolean passwordChanged) {
        if (currentUser == null || currentUser.getEmail() == null) return;
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), curPass);
        
        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean authChanged = emailChanged || passwordChanged;
                if (emailChanged) {
                    currentUser.updateEmail(email).addOnCompleteListener(taskEmail -> {
                        if (taskEmail.isSuccessful()) {
                            if (passwordChanged) {
                                currentUser.updatePassword(password).addOnCompleteListener(taskPass -> {
                                    if (taskPass.isSuccessful()) {
                                        updateDatabase(name, phone, address, email, true);
                                    } else {
                                        if (isAdded()) Toast.makeText(getContext(), "Password update failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                updateDatabase(name, phone, address, email, true);
                            }
                        } else {
                            if (isAdded()) Toast.makeText(getContext(), "Email update failed: " + taskEmail.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (passwordChanged) {
                    currentUser.updatePassword(password).addOnCompleteListener(taskPass -> {
                        if (taskPass.isSuccessful()) {
                            updateDatabase(name, phone, address, email, true);
                        } else {
                            if (isAdded()) Toast.makeText(getContext(), "Password update failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    updateDatabase(name, phone, address, email, false);
                }
            } else {
                if (isAdded()) Toast.makeText(getContext(), "Auth failed: Wrong password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDatabase(String name, String phone, String address, String email, boolean authChanged) {
        if (userRef == null) return;
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("address", address);
        updates.put("email", email);

        userRef.updateChildren(updates).addOnSuccessListener(unused -> {
            if (isAdded()) {
                Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                if (authChanged) {
                    logoutAndRedirect();
                } else {
                    getParentFragmentManager().popBackStack();
                }
            }
        }).addOnFailureListener(e -> {
            if (isAdded()) Toast.makeText(getContext(), "DB update failed", Toast.LENGTH_SHORT).show();
        });
    }

    private void showDeleteConfirmation() {
        EditText etPass = new EditText(getContext());
        etPass.setHint("Enter password to confirm");
        etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        int p = (int) (24 * getResources().getDisplayMetrics().density);
        etPass.setPadding(p, p, p, p);

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Permanently delete your account? Enter password to confirm.")
                .setView(etPass)
                .setPositiveButton("Delete Forever", (dialog, which) -> {
                    String password = etPass.getText().toString().trim();
                    if (!TextUtils.isEmpty(password)) {
                        reauthenticateAndDelete(password);
                    } else {
                        Toast.makeText(getContext(), "Password required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reauthenticateAndDelete(String password) {
        if (currentUser == null || currentUser.getEmail() == null) {
            Toast.makeText(getContext(), "Session error", Toast.LENGTH_SHORT).show();
            return;
        }
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (userRef != null) {
                    userRef.removeValue().addOnCompleteListener(dbTask -> {
                        currentUser.delete().addOnCompleteListener(authTask -> {
                            if (authTask.isSuccessful()) {
                                if (isAdded()) Toast.makeText(getContext(), "Account deleted", Toast.LENGTH_SHORT).show();
                                logoutAndRedirect();
                            } else {
                                if (isAdded()) Toast.makeText(getContext(), "Auth delete failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }
            } else {
                if (isAdded()) Toast.makeText(getContext(), "Wrong password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutAndRedirect() {
        FirebaseAuth.getInstance().signOut();
        if (getActivity() != null) {
            getActivity().getSharedPreferences("NovaPrefs", android.content.Context.MODE_PRIVATE)
                    .edit().clear().apply();
            Intent intent = new Intent(getActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}
