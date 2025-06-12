package com.example.movies_app.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.movies_app.Database.AppDatabase;
import com.example.movies_app.Database.entity.User;
import com.example.movies_app.Helper.PasswordHelper;
import com.example.movies_app.R;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEdt, passEdt;
    private Button loginBtn;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        database = AppDatabase.getInstance(this);

        TextView registerLink = findViewById(R.id.textView5);
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        initView();
    }

    private void initView() {
        emailEdt = findViewById(R.id.editTextText);
        passEdt = findViewById(R.id.editTextPassword);
        loginBtn = findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = emailEdt.getText().toString().trim();
        String password = passEdt.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                String hashedPassword = PasswordHelper.hashPassword(password);
                User user = database.userDao().loginUser(email, hashedPassword);

                runOnUiThread(() -> {
                    if (user != null) {
                        // Lưu thông tin đăng nhập
                        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
                        prefs.edit()
                                .putInt("user_id", user.getUserId())
                                .putString("username", user.getUsername())
                                .putString("email", user.getEmail())
                                .putString("role", user.getRole()) // ✅ THÊM ROLE
                                .putBoolean("is_logged_in", true)
                                .apply();

                        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                        // ✅ PHÂN QUYỀN CHUYỂN TRANG
                        Intent intent;
                        if (user.isAdmin()) {
                            intent = new Intent(LoginActivity.this, AdminActivity.class);
                        } else {
                            intent = new Intent(LoginActivity.this, MainActivity.class);
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Email hoặc mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Lỗi đăng nhập: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}