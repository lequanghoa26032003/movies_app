package com.example.movies_app.Activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.movies_app.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FavoriteActivity extends AppCompatActivity {

    private RecyclerView favoriteRecyclerView;

    // Bottom Navigation Components
    private ImageView btnHistory, btnFavorites, btnSearch, btnProfile;
    private FloatingActionButton fabHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        initViews();
        setupBottomNavigation();
        highlightCurrentTab();
        loadFavoriteMovies();
    }

    private void initViews() {
        favoriteRecyclerView = findViewById(R.id.favoriteRecyclerView);
        favoriteRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Bottom Navigation Views
        btnHistory = findViewById(R.id.btn_history);
        btnFavorites = findViewById(R.id.btn_favorites);
        btnSearch = findViewById(R.id.btn_search);
        btnProfile = findViewById(R.id.btn_profile);
        fabHome = findViewById(R.id.fab_home);
    }

    private void setupBottomNavigation() {
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
            finish();
        });

        btnFavorites.setOnClickListener(v -> {
            // Đã ở Favorites rồi, không làm gì
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
        // Reset all icons to white
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        int selectedColor = ContextCompat.getColor(this, R.color.selected_tab_color);

        btnHistory.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnFavorites.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN); // ✅ Highlight Favorites
        btnSearch.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnProfile.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
    }

    private void loadFavoriteMovies() {
        // TODO: Load danh sách phim yêu thích
    }
}