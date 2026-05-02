package com.example.nova_ecommerce.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.admin.activities.AdminDashboard;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private CheckBox cbRememberMe;
    private RadioGroup rgRole;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    private static final String PREF_NAME = "NovaPrefs";
    private static final String KEY_REMEMBER = "isRemembered";
    private static final String KEY_ROLE = "userRole";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        rgRole = findViewById(R.id.rgRole);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvSignUp = findViewById(R.id.tvGoToSignUp);

        // Auto-login logic
        if (mAuth.getCurrentUser() != null && sharedPreferences.getBoolean(KEY_REMEMBER, false)) {
            redirectBasedOnRole(sharedPreferences.getString(KEY_ROLE, "user"));
        }

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            int selectedRoleId = rgRole.getCheckedRadioButtonId();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    checkUserRoleFromDatabase(mAuth.getCurrentUser().getUid(), selectedRoleId);
                } else {
                    Toast.makeText(this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        tvSignUp.setOnClickListener(v -> startActivity(new Intent(Login.this, SignUp.class)));
    }

    private void checkUserRoleFromDatabase(String uid, int selectedRoleId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String roleInDb = snapshot.child("role").getValue(String.class);

                    if (selectedRoleId == R.id.rbAdmin && "admin".equals(roleInDb)) {
                        handleLoginSuccess("admin");
                    } else if (selectedRoleId == R.id.rbUser && "user".equals(roleInDb)) {
                        handleLoginSuccess("user");
                    } else {
                        mAuth.signOut();
                        Toast.makeText(Login.this, "Access Denied: Incorrect Role Selected", Toast.LENGTH_LONG).show();
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
        if ("admin".equals(role)) {
            startActivity(new Intent(Login.this, AdminDashboard.class));
        } else {
            startActivity(new Intent(Login.this, Dashboard.class));
        }
    }
}