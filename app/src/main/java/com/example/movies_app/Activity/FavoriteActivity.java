package com.example.movies_app.Activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movies_app.Adapter.EmptyAdapter;
import com.example.movies_app.Helper.BaseBottomNavigationHelper;
import com.example.movies_app.R;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FavoriteActivity extends AppCompatActivity {

    private RecyclerView favoriteRecyclerView;

    // Bottom Navigation Components
    private ImageView btnMain, btnHistory, btnFavorites, btnSearch, btnProfile;
    private FloatingActionButton fabHome;
    private BottomAppBar bottomAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        initViews();
        setupBottomNavigation();
        highlightCurrentTab();
        loadFavoriteMovies();
        setFabToFavoritePosition();
    }

    private void initViews() {
        favoriteRecyclerView = findViewById(R.id.favoriteRecyclerView);
        favoriteRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        favoriteRecyclerView.setAdapter(new EmptyAdapter());
        bottomAppBar = findViewById(R.id.app_bar);

        // Bottom Navigation Views
        btnHistory = findViewById(R.id.btn_history);
        btnFavorites = findViewById(R.id.btn_favorites);
        btnSearch = findViewById(R.id.btn_search);
        btnProfile = findViewById(R.id.btn_profile);
        fabHome = findViewById(R.id.fab_home);
        btnMain = findViewById(R.id.btn_center);
    }

    private void setupBottomNavigation() {
        btnHistory.setOnClickListener(v -> {
            BaseBottomNavigationHelper.setFabPosition(
                    bottomAppBar,
                    fabHome,
                    BaseBottomNavigationHelper.HISTORY_POSITION
            );

            fabHome.postDelayed(() -> {
                Intent intent = new Intent(this, HistoryActivity.class);
                startActivity(intent);
            }, 200); // Giảm delay xuống 200ms
        });

        btnSearch.setOnClickListener(v -> {
            BaseBottomNavigationHelper.setFabPosition(
                    bottomAppBar,
                    fabHome,
                    BaseBottomNavigationHelper.SEARCH_POSITION
            );

            fabHome.postDelayed(() -> {
                Intent intent = new Intent(this, ExploreActivity.class);
                startActivity(intent);
            }, 200);
        });

        btnMain.setOnClickListener(v -> {
            BaseBottomNavigationHelper.setFabPosition(
                    bottomAppBar,
                    fabHome,
                    BaseBottomNavigationHelper.CENTER_POSITION
            );

            fabHome.postDelayed(() -> {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }, 200);
        });

        btnProfile.setOnClickListener(v -> {
            BaseBottomNavigationHelper.setFabPosition(
                    bottomAppBar,
                    fabHome,
                    BaseBottomNavigationHelper.PROFILE_POSITION
            );

            fabHome.postDelayed(() -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
            }, 200);
        });
    }
    private void setFabToFavoritePosition() {
        BaseBottomNavigationHelper.setFabPositionImmediate(
                bottomAppBar,
                fabHome,
                BaseBottomNavigationHelper.FAVORITES_POSITION
        );
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
        // TODO: Load danh sách phim yêu thích thực tế sau
        // Adapter đã được gán trong initViews() rồi
    }
}