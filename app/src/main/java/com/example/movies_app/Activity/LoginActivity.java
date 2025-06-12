package com.example.movies_app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.movies_app.R;

public class LoginActivity extends AppCompatActivity{
    private EditText userEdt, passEdt;
    private Button loginBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        TextView registerLink = findViewById(R.id.textView5);
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        initView();


    }

    private void initView() {
        userEdt = findViewById(R.id.editTextText);
        passEdt = findViewById(R.id.editTextPassword);
        loginBtn = findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(v -> {
            if(userEdt.getText().toString().isEmpty()|| passEdt.getText().toString().isEmpty()){
                Toast.makeText(LoginActivity.this, "Please fill you user and password", Toast.LENGTH_SHORT).show();
            }else if (userEdt.getText().toString().equals("admin") && passEdt.getText().toString().equals("admin")) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
        });
    }
}