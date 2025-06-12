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
import com.example.movies_app.Helper.BaseBottomNavigationHelper;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public abstract class BaseActivity extends AppCompatActivity {

    protected ImageView btnMain,btnHistory, btnFavorites, btnSearch, btnProfile;
    protected FloatingActionButton fabHome;
    protected BottomAppBar bottomAppBar;
    protected ConstraintLayout mainContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        setContentView(R.layout.activity_base);
        mainContainer = findViewById(R.id.content_container);
        LayoutInflater.from(this).inflate(getContentLayoutId(), mainContainer, true);

        initBottomNavigation();
        setupBottomNavigation();
        setupCurrentTabFabPosition();
        highlightCurrentTab(); // ✅ Method này cần được thêm vào
    }

    // Method abstract để activity con override
    protected abstract int getContentLayoutId();
    protected abstract String getCurrentTab();

    private void initBottomNavigation() {
        btnHistory = findViewById(R.id.btn_history);
        btnFavorites = findViewById(R.id.btn_favorites);
        btnSearch = findViewById(R.id.btn_search);
        btnProfile = findViewById(R.id.btn_profile);
        btnMain = findViewById(R.id.btn_center);
        fabHome = findViewById(R.id.fab_home);
        bottomAppBar = findViewById(R.id.app_bar);
    }

    private void setupBottomNavigation() {
        btnHistory.setOnClickListener(v -> navigateToActivity(HistoryActivity.class));
        btnFavorites.setOnClickListener(v -> navigateToActivity(FavoriteActivity.class));
        btnSearch.setOnClickListener(v -> navigateToActivity(ExploreActivity.class));
        btnProfile.setOnClickListener(v -> navigateToActivity(ProfileActivity.class));
        fabHome.setOnClickListener(v -> navigateToActivity(MainActivity.class));
    }

    private void setupCurrentTabFabPosition() {
        String currentTab = getCurrentTab();
        float position;

        switch (currentTab) {
            case "history":
                position = BaseBottomNavigationHelper.HISTORY_POSITION;
                break;
            case "favorites":
                position = BaseBottomNavigationHelper.FAVORITES_POSITION;
                break;
            case "search":
                position = BaseBottomNavigationHelper.SEARCH_POSITION;
                break;
            case "profile":
                position = BaseBottomNavigationHelper.PROFILE_POSITION;
                break;
            case "home":
            default:
                position = BaseBottomNavigationHelper.CENTER_POSITION;
                break;
        }

        BaseBottomNavigationHelper.setFabPositionImmediate(bottomAppBar, fabHome, position);
    }

    private void navigateToActivity(Class<?> activityClass) {
        if (!this.getClass().equals(activityClass)) {
            Intent intent = new Intent(this, activityClass);
            startActivity(intent);
            finish();
        }
    }

    // ✅ THÊM 2 METHOD NÀY VÀO:
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}