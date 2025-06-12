package com.example.movies_app.Activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.movies_app.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView userName, userEmail;

    // Bottom Navigation Components
    private ImageView btnHistory, btnFavorites, btnSearch, btnProfile;
    private FloatingActionButton fabHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupBottomNavigation();
        highlightCurrentTab();
        loadUserProfile();
    }

    private void initViews() {
        profileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);

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
            // Đã ở Profile rồi, không làm gì
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

        btnHistory.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnFavorites.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnSearch.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnProfile.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN); // ✅ Highlight Profile
    }

    private void loadUserProfile() {
        userName.setText("nguyenanhtai2003");
        userEmail.setText("nguyenanhtai2003@gmail.com");
    }
}