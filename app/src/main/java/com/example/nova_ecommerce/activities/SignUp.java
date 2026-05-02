package com.example.nova_ecommerce.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nova_ecommerce.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    private EditText     etName, etEmail, etPassword, etConfirmPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth             = FirebaseAuth.getInstance();
        etName            = findViewById(R.id.etFullName);
        etEmail           = findViewById(R.id.etEmail);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        Button   btnSignUp    = findViewById(R.id.btnSignUp);
        TextView tvLoginLink  = findViewById(R.id.tvGoToLogin);

        tvLoginLink.setOnClickListener(v -> finish());
        btnSignUp.setOnClickListener(v -> validateAndRegister());
    }

    private void validateAndRegister() {
        String name        = etName.getText().toString().trim();
        String email       = etEmail.getText().toString().trim();
        String pass        = etPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this,
                    "All fields are required",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.length() < 6) {
            Toast.makeText(this,
                    "Password must be at least 6 characters",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pass.equals(confirmPass)) {
            Toast.makeText(this,
                    "Passwords do not match!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        saveUserToDatabase(uid, name, email);
                    } else {
                        Toast.makeText(this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToDatabase(String uid, String name, String email) {
        String createdAt = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        // Match exact structure of your DB users node
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name",         name);
        userMap.put("email",        email);
        userMap.put("role",         "user");
        userMap.put("phone",        "");
        userMap.put("address",      "");
        userMap.put("profileImage", "https://via.placeholder.com/150");
        userMap.put("createdAt",    createdAt);

        FirebaseDatabase.getInstance(
                        "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com"
                ).getReference("users")   // ← lowercase "users" to match your DB
                .child(uid)
                .setValue(userMap)
                .addOnSuccessListener(unused -> {
                    // Send the verification email configured in your console
                    if (mAuth.getCurrentUser() != null) {
                        mAuth.getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(emailTask -> {
                                    if (emailTask.isSuccessful()) {
                                        Toast.makeText(this,
                                                "Verification email sent! Please check your Gmail.",
                                                Toast.LENGTH_LONG).show();
                                    }

                                    // CRITICAL: Sign out immediately so they can't enter yet
                                    mAuth.signOut();
                                    finish(); // Go back to login
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Account created but profile save failed: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}