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
import com.example.nova_ecommerce.utils.OtpManager;
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

    private static final String DB_URL =
            "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com";

    private ImageView         imgEditAvatar;
    private TextInputEditText etName, etEmail, etPhone, etAddress, etPassword;
    private Button            btnSave;
    private ImageView         btnBack;
    private TextView          tvDeleteAccount, tvLogout;

    private FirebaseAuth      mAuth;
    private DatabaseReference userRef;
    private FirebaseUser      currentUser;
    private String            userId;

    private String currentName, currentEmail, currentPhone,
            currentAddress, currentImageUrl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_edit_profile, container, false);

        mAuth       = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId  = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance(DB_URL)
                    .getReference("users").child(userId);
        }

        imgEditAvatar   = view.findViewById(R.id.imgEditAvatar);
        etName          = view.findViewById(R.id.etEditName);
        etEmail         = view.findViewById(R.id.etEditEmail);
        etPhone         = view.findViewById(R.id.etEditPhone);
        etAddress       = view.findViewById(R.id.etEditAddress);
        etPassword      = view.findViewById(R.id.etEditPassword);
        btnSave         = view.findViewById(R.id.btnSaveProfile);
        btnBack         = view.findViewById(R.id.btnBack);
        tvDeleteAccount = view.findViewById(R.id.tvDeleteAccount);
        tvLogout        = view.findViewById(R.id.tvLogout);

        loadUserData();

        btnBack.setOnClickListener(v ->
                getParentFragmentManager().popBackStack());
        btnSave.setOnClickListener(v -> validateAndSave());
        tvDeleteAccount.setOnClickListener(v -> showDeleteConfirmation());
        tvLogout.setOnClickListener(v -> logoutAndRedirect());

        return view;
    }

    // ── Load profile from DB ──────────────────────────────────
    private void loadUserData() {
        if (userRef == null) return;
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                currentName     = snap.child("name").getValue(String.class);
                currentEmail    = snap.child("email").getValue(String.class);
                currentPhone    = snap.child("phone").getValue(String.class);
                currentAddress  = snap.child("address").getValue(String.class);
                currentImageUrl = snap.child("profileImage").getValue(String.class);

                etName.setText(currentName);
                etEmail.setText(currentEmail);
                etPhone.setText(currentPhone);
                etAddress.setText(currentAddress);

                if (!TextUtils.isEmpty(currentImageUrl)
                        && !currentImageUrl.contains("placeholder")
                        && isAdded()) {
                    Glide.with(requireContext())
                            .load(currentImageUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_person)
                            .into(imgEditAvatar);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    // ── Route by what changed ─────────────────────────────────
    private void validateAndSave() {
        String newName     = etName.getText().toString().trim();
        String newEmail    = etEmail.getText().toString().trim();
        String newPhone    = etPhone.getText().toString().trim();
        String newAddress  = etAddress.getText().toString().trim();
        String newPassword = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newName)) {
            Toast.makeText(getContext(),
                    "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean emailChanged    = !newEmail.equalsIgnoreCase(currentEmail);
        boolean phoneChanged    = !TextUtils.equals(newPhone, currentPhone);
        boolean passwordChanged = !TextUtils.isEmpty(newPassword);
        boolean infoOnly        = !newName.equals(currentName)
                || !TextUtils.equals(newAddress, currentAddress);

        if (!emailChanged && !phoneChanged && !passwordChanged && !infoOnly) {
            Toast.makeText(getContext(),
                    "No changes detected", Toast.LENGTH_SHORT).show();
            return;
        }
        if (emailChanged && !android.util.Patterns.EMAIL_ADDRESS
                .matcher(newEmail).matches()) {
            Toast.makeText(getContext(),
                    "Invalid email address", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phoneChanged && newPhone.length() < 10) {
            Toast.makeText(getContext(),
                    "Invalid phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (passwordChanged && newPassword.length() < 6) {
            Toast.makeText(getContext(),
                    "Password must be at least 6 characters",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // CASE 1: Password only — old password dialog → update Auth + DB → Login
        if (passwordChanged && !emailChanged && !phoneChanged) {
            showOldPasswordDialog(oldPass ->
                    reauthThen(oldPass, () ->
                            doPasswordUpdate(newPassword, newName,
                                    newPhone, newAddress)));
            return;
        }

        // CASE 2: Email or phone — old password dialog → OTP → update Auth + DB → Login
        if (emailChanged || phoneChanged) {
            String otpTargetEmail = emailChanged ? newEmail : currentEmail;
            String displayTarget  = emailChanged
                    ? "new email " + newEmail
                    : "email " + currentEmail;

            showOldPasswordDialog(oldPass ->
                    reauthThen(oldPass, () ->
                            sendOtpAndVerify(otpTargetEmail, displayTarget,
                                    newName, newEmail, newPhone,
                                    newAddress, emailChanged)));
            return;
        }

        // CASE 3: Name/address only — no re-auth, no redirect
        updateDatabase(newName, newPhone, newAddress,
                currentEmail, false, null);
    }

    // ── CASE 1: update Auth password then DB ──────────────────
    private void doPasswordUpdate(String newPassword, String name,
                                  String phone, String address) {
        currentUser.updatePassword(newPassword)
                .addOnSuccessListener(u ->
                        updateDatabase(name, phone, address,
                                currentEmail, true, newPassword))
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(getContext(),
                            "Password update failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ── CASE 2: send OTP then show verify dialog ──────────────
    private void sendOtpAndVerify(String otpEmail, String displayTarget,
                                  String name, String newEmail,
                                  String newPhone, String address,
                                  boolean emailChanged) {
        // Generate + save OTP to Firebase otps/{userId}
        String otp = OtpManager.generateAndSave(userId);

        if (isAdded()) Toast.makeText(requireContext(),
                "Sending OTP to " + displayTarget + "…",
                Toast.LENGTH_SHORT).show();

        OtpManager.sendOtpEmail(otpEmail, currentName, otp,
                new OtpManager.OtpEmailCallback() {
                    @Override
                    public void onSuccess() {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() ->
                                showOtpVerifyDialog(otp, name, newEmail,
                                        newPhone, address, emailChanged));
                    }

                    @Override
                    public void onFailure(String error) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(),
                                        "Failed to send OTP: " + error,
                                        Toast.LENGTH_LONG).show());
                    }
                });
    }

    // ── OTP entry dialog ──────────────────────────────────────
    private void showOtpVerifyDialog(String expectedOtp,
                                     String name, String newEmail,
                                     String newPhone, String address,
                                     boolean emailChanged) {
        EditText etOtp = new EditText(getContext());
        etOtp.setHint("Enter 6-digit OTP");
        etOtp.setInputType(InputType.TYPE_CLASS_NUMBER);
        etOtp.setMaxLines(1);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        etOtp.setPadding(pad, pad, pad, pad);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Verify OTP")
                .setMessage("Enter the 6-digit code sent to your email.")
                .setView(etOtp)
                .setCancelable(false)
                .setPositiveButton("Verify", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            // Override positive button to prevent auto-dismiss on wrong OTP
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        String entered = etOtp.getText().toString().trim();
                        if (TextUtils.isEmpty(entered)) {
                            etOtp.setError("Enter the OTP");
                            return;
                        }
                        if (!entered.equals(expectedOtp)) {
                            etOtp.setError("Incorrect OTP");
                            return;
                        }
                        // ✅ OTP correct
                        dialog.dismiss();
                        if (emailChanged) {
                            // Re-auth fresh before updateEmail —
                            // the token from the earlier re-auth may have
                            // expired by the time the user entered the OTP
                            showOldPasswordDialog(oldPass -> {
                                AuthCredential freshCred =
                                        EmailAuthProvider.getCredential(
                                                currentUser.getEmail(), oldPass);
                                currentUser.reauthenticate(freshCred)
                                        .addOnSuccessListener(u ->
                                                currentUser.updateEmail(newEmail)
                                                        .addOnSuccessListener(v2 ->
                                                                updateDatabase(
                                                                        name, newPhone,
                                                                        address, newEmail,
                                                                        true, null))
                                                        .addOnFailureListener(e -> {
                                                            if (isAdded())
                                                                Toast.makeText(getContext(),
                                                                        "Email update failed: "
                                                                                + e.getMessage(),
                                                                        Toast.LENGTH_LONG).show();
                                                        }))
                                        .addOnFailureListener(e -> {
                                            if (isAdded())
                                                Toast.makeText(getContext(),
                                                        "Incorrect password",
                                                        Toast.LENGTH_SHORT).show();
                                        });
                            });
                        } else {
                            // Phone only — DB update is enough
                            updateDatabase(name, newPhone, address,
                                    currentEmail, true, null);
                        }
                    });
        });

        dialog.show();
    }

    // ── Re-auth with current password ─────────────────────────
    private void reauthThen(String oldPassword, Runnable onSuccess) {
        if (currentUser == null || currentUser.getEmail() == null) return;
        AuthCredential cred = EmailAuthProvider.getCredential(
                currentUser.getEmail(), oldPassword);
        currentUser.reauthenticate(cred)
                .addOnSuccessListener(u -> onSuccess.run())
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(getContext(),
                            "Incorrect password",
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ── Ask for current password ──────────────────────────────
    private void showOldPasswordDialog(
            java.util.function.Consumer<String> onConfirmed) {
        EditText etPass = new EditText(getContext());
        etPass.setHint("Current password");
        etPass.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        etPass.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm your password")
                .setMessage("Enter your current password to continue.")
                .setView(etPass)
                .setPositiveButton("Confirm", (d, w) -> {
                    String pass = etPass.getText().toString().trim();
                    if (TextUtils.isEmpty(pass)) {
                        Toast.makeText(getContext(),
                                "Password required",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        onConfirmed.accept(pass);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Write to DB, optionally sign out → Login ──────────────
    private void updateDatabase(String name, String phone, String address,
                                String email, boolean redirectToLogin,
                                @Nullable String newPassword) {
        if (userRef == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("name",    name);
        updates.put("phone",   phone);
        updates.put("address", address);
        updates.put("email",   email);
        if (newPassword != null) updates.put("password", newPassword);

        userRef.updateChildren(updates)
                .addOnSuccessListener(u -> {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(),
                            redirectToLogin
                                    ? "Changes saved. Please log in again."
                                    : "Profile updated.",
                            Toast.LENGTH_SHORT).show();
                    if (redirectToLogin) logoutAndRedirect();
                    else getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(getContext(),
                            "DB update failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ── Delete account ────────────────────────────────────────
    private void showDeleteConfirmation() {
        EditText etPass = new EditText(getContext());
        etPass.setHint("Enter password to confirm");
        etPass.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        etPass.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("This permanently deletes your account and all data.")
                .setView(etPass)
                .setPositiveButton("Delete Forever", (d, w) -> {
                    String pass = etPass.getText().toString().trim();
                    if (!TextUtils.isEmpty(pass)) reauthenticateAndDelete(pass);
                    else Toast.makeText(getContext(),
                            "Password required", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reauthenticateAndDelete(String password) {
        if (currentUser == null || currentUser.getEmail() == null) return;
        AuthCredential cred = EmailAuthProvider.getCredential(
                currentUser.getEmail(), password);
        currentUser.reauthenticate(cred)
                .addOnSuccessListener(u -> {
                    if (userRef != null) {
                        userRef.removeValue().addOnCompleteListener(t ->
                                currentUser.delete()
                                        .addOnSuccessListener(v2 -> {
                                            if (isAdded()) Toast.makeText(
                                                    getContext(),
                                                    "Account deleted",
                                                    Toast.LENGTH_SHORT).show();
                                            logoutAndRedirect();
                                        })
                                        .addOnFailureListener(e -> {
                                            if (isAdded()) Toast.makeText(
                                                    getContext(),
                                                    "Delete failed: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }));
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(getContext(),
                            "Wrong password", Toast.LENGTH_SHORT).show();
                });
    }

    // ── Sign out → Login ──────────────────────────────────────
    private void logoutAndRedirect() {
        FirebaseAuth.getInstance().signOut();
        if (getActivity() != null) {
            getActivity()
                    .getSharedPreferences("NovaPrefs",
                            android.content.Context.MODE_PRIVATE)
                    .edit().clear().apply();
            Intent intent = new Intent(getActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}
