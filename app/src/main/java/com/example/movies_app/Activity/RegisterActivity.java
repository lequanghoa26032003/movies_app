package com.example.movies_app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.movies_app.Database.AppDatabase;
import com.example.movies_app.Database.entity.User;
import com.example.movies_app.Database.entity.UserPreference;
import com.example.movies_app.Helper.PasswordHelper;
import com.example.movies_app.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameEdt, emailEdt, passwordEdt;
    private Button registerBtn;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        database = AppDatabase.getInstance(this);
        initView();
    }

    private void initView() {
        usernameEdt = findViewById(R.id.registerUsername);
        emailEdt = findViewById(R.id.registerEmail);
        passwordEdt = findViewById(R.id.registerPassword);
        registerBtn = findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = usernameEdt.getText().toString().trim();
        String email = emailEdt.getText().toString().trim();
        String password = passwordEdt.getText().toString().trim();

        // Validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra trong database
        new Thread(() -> {
            try {
                // Kiểm tra email và username đã tồn tại chưa
                if (database.userDao().checkEmailExists(email) > 0) {
                    runOnUiThread(() -> Toast.makeText(this, "Email đã được sử dụng", Toast.LENGTH_SHORT).show());
                    return;
                }

                if (database.userDao().checkUsernameExists(username) > 0) {
                    runOnUiThread(() -> Toast.makeText(this, "Tên đăng nhập đã được sử dụng", Toast.LENGTH_SHORT).show());
                    return;
                }

                // Tạo user mới
                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String hashedPassword = PasswordHelper.hashPassword(password);

                User newUser = new User(email, username, hashedPassword, username, "", currentDate);
                long userId = database.userDao().insertUser(newUser);

                // Tạo user preference mặc định
                UserPreference userPrefs = new UserPreference((int) userId);
                database.userDao().insertUserPreference(userPrefs);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Lỗi đăng ký: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}