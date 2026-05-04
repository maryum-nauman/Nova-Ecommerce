package com.example.nova_ecommerce.user.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.admin.activities.AdminDashboard;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private EditText         etEmail, etPassword;
    private CheckBox         cbRememberMe;
    private TextView         tvRoleUser, tvRoleAdmin;
    private FirebaseAuth     mAuth;
    private SharedPreferences sharedPreferences;

    private String selectedRole = "user";

    private static final String PREF_NAME    = "NovaPrefs";
    private static final String KEY_REMEMBER = "isRemembered";
    private static final String KEY_ROLE     = "userRole";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth             = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        etEmail      = findViewById(R.id.etEmail);
        etPassword   = findViewById(R.id.etPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        tvRoleUser   = findViewById(R.id.tvRoleUser);
        tvRoleAdmin  = findViewById(R.id.tvRoleAdmin);
        Button   btnLogin    = findViewById(R.id.btnLogin);
        TextView tvSignUp    = findViewById(R.id.tvGoToSignUp);
        TextView tvForgotPass = findViewById(R.id.tvForgotPass);

        if (mAuth.getCurrentUser() != null
                && sharedPreferences.getBoolean(KEY_REMEMBER, false)) {
            redirectBasedOnRole(
                    sharedPreferences.getString(KEY_ROLE, "user"));
            return;
        }

        selectRole("user");
        tvRoleUser.setOnClickListener(v  -> selectRole("user"));
        tvRoleAdmin.setOnClickListener(v -> selectRole("admin"));

        tvForgotPass.setOnClickListener(v -> showForgotPasswordDialog());

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this,
                        "Please fill all fields",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            com.google.firebase.auth.FirebaseUser user = mAuth.getCurrentUser();
                            if (user == null) return;

                            if ("admin".equals(selectedRole)) {
                                checkUserRoleFromDatabase(user.getUid());
                            } else {
                                user.reload().addOnCompleteListener(reload -> {
                                            if (user.isEmailVerified()) {checkUserRoleFromDatabase(user.getUid());
                                            } else {
                                                mAuth.signOut();
                                                Toast.makeText(this, "Please verify your email first!", Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(this, "Login failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(Login.this, SignUp.class)));
    }

    private void selectRole(String role) {
        selectedRole = role;

        if ("user".equals(role)) {
            tvRoleUser.setBackgroundResource(R.drawable.role_card_selected);
            tvRoleUser.setTextColor(getColor(R.color.colorPrimary));
            tvRoleAdmin.setBackgroundResource(R.drawable.role_card_unselected);
            tvRoleAdmin.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        } else {
            tvRoleAdmin.setBackgroundResource(R.drawable.role_card_selected);
            tvRoleAdmin.setTextColor(getColor(R.color.colorPrimary));
            tvRoleUser.setBackgroundResource(R.drawable.role_card_unselected);
            tvRoleUser.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        }
    }

    private void showForgotPasswordDialog() {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        EditText etResetEmail = dialogView.findViewById(R.id.etResetEmail);

        String currentEmail = etEmail.getText().toString().trim();
        if (!currentEmail.isEmpty()) {etResetEmail.setText(currentEmail);
        }

        new MaterialAlertDialogBuilder(this,
                R.style.MaterialAlertDialog_Nova)
                .setTitle("Reset Password")
                .setIcon(android.R.drawable.ic_lock_idle_lock)
                .setView(dialogView)
                .setPositiveButton("Send Reset Link", (dialog, which) -> {
                    String resetEmail = etResetEmail.getText().toString().trim();

                    if (resetEmail.isEmpty() || !resetEmail.contains("@")) {
                        Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sendPasswordResetEmail(resetEmail);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused ->
                        new MaterialAlertDialogBuilder(this,
                                R.style.MaterialAlertDialog_Nova)
                                .setTitle("Email Sent ✅")
                                .setMessage("A password reset link has been sent to:\n\n" + email
                                        + "\n\nCheck your inbox and follow the link to reset your password.")
                                .setPositiveButton("OK", null)
                                .show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void checkUserRoleFromDatabase(String uid) {
        DatabaseReference ref = FirebaseDatabase.getInstance("https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com").getReference("users").child(uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String roleInDb = snapshot.child("role").getValue(String.class);

                    if (selectedRole.equals(roleInDb)) {
                        handleLoginSuccess(roleInDb);
                    } else {
                        mAuth.signOut();
                        Toast.makeText(Login.this, "Access Denied: Incorrect role selected", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void handleLoginSuccess(String role) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_REMEMBER, cbRememberMe.isChecked());
        editor.putString(KEY_ROLE, role);
        editor.apply();
        redirectBasedOnRole(role);
    }

    private void redirectBasedOnRole(String role) {
        Intent intent = "admin".equals(role)
                ? new Intent(Login.this, AdminDashboard.class)
                : new Intent(Login.this, Dashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}