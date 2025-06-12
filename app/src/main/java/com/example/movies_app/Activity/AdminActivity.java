package com.example.movies_app.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.movies_app.R;

public class AdminActivity extends AppCompatActivity {
    private TextView welcomeText;
    private CardView manageMoviesCard, manageUsersCard, statisticsCard, settingsCard;
    private Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        initViews();
        setupClickListeners();

        // Hiển thị thông tin admin
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String username = prefs.getString("username", "Admin");
        welcomeText.setText("Chào mừng Admin: " + username);
    }

    private void initViews() {
        welcomeText = findViewById(R.id.welcomeText);
        manageMoviesCard = findViewById(R.id.manageMoviesCard);
        manageUsersCard = findViewById(R.id.manageUsersCard);
        statisticsCard = findViewById(R.id.statisticsCard);
        settingsCard = findViewById(R.id.settingsCard);
        logoutBtn = findViewById(R.id.logoutBtn);
    }

    private void setupClickListeners() {
        // Quản lý phim
        manageMoviesCard.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, ManageMoviesActivity.class);
            startActivity(intent);
        });

        // Quản lý người dùng
        manageUsersCard.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, ManageUsersActivity.class);
            startActivity(intent);
        });

        // Thống kê
        statisticsCard.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });

        // Cài đặt
        settingsCard.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminSettingsActivity.class);
            startActivity(intent);
        });

        // Đăng xuất
        logoutBtn.setOnClickListener(v -> logout());
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Không cho phép quay lại bằng nút back
        moveTaskToBack(true);
    }
}