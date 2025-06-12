package com.example.movies_app.Activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.movies_app.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private TextView emptyTextView;

    // Bottom Navigation Components
    private ImageView btnHistory, btnFavorites, btnSearch, btnProfile;
    private FloatingActionButton fabHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initViews();
        setupBottomNavigation();
        highlightCurrentTab();
        loadWatchHistory();
    }

    private void initViews() {
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        emptyTextView = findViewById(R.id.emptyTextView);

        // Bottom Navigation Views
        btnHistory = findViewById(R.id.btn_history);
        btnFavorites = findViewById(R.id.btn_favorites);
        btnSearch = findViewById(R.id.btn_search);
        btnProfile = findViewById(R.id.btn_profile);
        fabHome = findViewById(R.id.fab_home);
    }

    private void setupBottomNavigation() {
        btnHistory.setOnClickListener(v -> {
            // Đã ở History rồi, không làm gì
        });

        btnFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(this, FavoriteActivity.class);
            startActivity(intent);
            finish();
        });

        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExploreActivity.class);
            startActivity(intent);
            finish();
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

        fabHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void highlightCurrentTab() {
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        int selectedColor = ContextCompat.getColor(this, R.color.selected_tab_color);

        btnHistory.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN); // ✅ Highlight History
        btnFavorites.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnSearch.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnProfile.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
    }

    private void loadWatchHistory() {
        // TODO: Load lịch sử xem phim từ SharedPreferences hoặc database
        // Tạm thời hiển thị empty state
        emptyTextView.setText("📺\n\nChưa có lịch sử xem phim nào\n\nHãy bắt đầu xem những bộ phim yêu thích!");
    }
}