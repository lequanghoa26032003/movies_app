package com.example.movies_app.Activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import com.example.movies_app.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public abstract class BaseActivity extends AppCompatActivity {

    protected ImageView btnHistory, btnFavorites, btnSearch, btnProfile;
    protected FloatingActionButton fabHome;
    protected ConstraintLayout mainContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tạo layout chính với bottom navigation
        setContentView(R.layout.activity_base);

        // Inflate nội dung con vào container
        mainContainer = findViewById(R.id.content_container);
        LayoutInflater.from(this).inflate(getContentLayoutId(), mainContainer, true);

        initBottomNavigation();
        setupBottomNavigation();
        highlightCurrentTab();
    }

    // Method abstract để activity con override
    protected abstract int getContentLayoutId();
    protected abstract String getCurrentTab();

    private void initBottomNavigation() {
        btnHistory = findViewById(R.id.btn_history);
        btnFavorites = findViewById(R.id.btn_favorites);
        btnSearch = findViewById(R.id.btn_search);
        btnProfile = findViewById(R.id.btn_profile);
        fabHome = findViewById(R.id.fab_home);
    }

    private void setupBottomNavigation() {
        btnHistory.setOnClickListener(v -> navigateToActivity(HistoryActivity.class));
        btnFavorites.setOnClickListener(v -> navigateToActivity(FavoriteActivity.class));
        btnSearch.setOnClickListener(v -> navigateToActivity(ExploreActivity.class));
        btnProfile.setOnClickListener(v -> navigateToActivity(ProfileActivity.class));
        fabHome.setOnClickListener(v -> navigateToActivity(MainActivity.class));
    }

    private void navigateToActivity(Class<?> activityClass) {
        if (!this.getClass().equals(activityClass)) {
            Intent intent = new Intent(this, activityClass);
            startActivity(intent);
            finish();
        }
    }

    private void highlightCurrentTab() {
        resetAllIcons();

        String currentTab = getCurrentTab();
        int selectedColor = ContextCompat.getColor(this, R.color.selected_tab_color);

        switch (currentTab) {
            case "history":
                btnHistory.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN);
                break;
            case "favorites":
                btnFavorites.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN);
                break;
            case "search":
                btnSearch.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN);
                break;
            case "profile":
                btnProfile.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN);
                break;
            case "home":
                // FAB có thể có style riêng
                break;
        }
    }

    private void resetAllIcons() {
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        btnHistory.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnFavorites.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnSearch.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnProfile.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
    }
}