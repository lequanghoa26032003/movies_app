package com.example.movies_app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.movies_app.R;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameEdt, emailEdt, passwordEdt;
    private Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
    }

    private void initView() {
        usernameEdt = findViewById(R.id.registerUsername);
        emailEdt = findViewById(R.id.registerEmail);
        passwordEdt = findViewById(R.id.registerPassword);
        registerBtn = findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(v -> {
            String username = usernameEdt.getText().toString();
            String email = emailEdt.getText().toString();
            String password = passwordEdt.getText().toString();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: Save user data (e.g., to a database or shared preferences)
                Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}