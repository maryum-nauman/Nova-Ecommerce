package com.example.nova_ecommerce.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nova_ecommerce.R;

public class Onboarding extends AppCompatActivity {

    private Button btnGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Initialize view
        btnGetStarted = findViewById(R.id.btnGetStarted);

        // Navigation logic (Equivalent to navigation.navigate('Login'))
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Onboarding.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
    }
}