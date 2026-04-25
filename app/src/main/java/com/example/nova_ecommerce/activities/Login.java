package com.example.nova_ecommerce.activities;

import android.content.Intent;
import android.content.SharedPreferences; // Added
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox; // Added
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nova_ecommerce.R;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private CheckBox cbRememberMe; // Added
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences; // Added
    private static final String PREF_NAME = "NovaPrefs";
    private static final String KEY_REMEMBER = "isRemembered";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe); // Added
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvSignUp = findViewById(R.id.tvGoToSignUp);

        // CHECK IF ALREADY LOGGED IN VIA REMEMBER ME
        boolean isRemembered = sharedPreferences.getBoolean(KEY_REMEMBER, false);
        if (mAuth.getCurrentUser() != null && isRemembered) {
            startActivity(new Intent(Login.this, Dashboard.class));
            finish();
        }

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // SAVE REMEMBER ME STATE
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(KEY_REMEMBER, cbRememberMe.isChecked());
                            editor.apply();

                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Login.this, Dashboard.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, SignUp.class));
        });
    }
}