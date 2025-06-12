    package com.example.movies_app.Activity;

    import android.content.Intent;
    import android.graphics.PorterDuff;
    import android.os.Bundle;
    import android.widget.ImageView;
    import android.widget.TextView;

    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.content.ContextCompat;
    import androidx.recyclerview.widget.RecyclerView;

    import com.example.movies_app.Helper.BaseBottomNavigationHelper;
    import com.example.movies_app.R;
    import com.google.android.material.bottomappbar.BottomAppBar;
    import com.google.android.material.floatingactionbutton.FloatingActionButton;

    public class HistoryActivity extends AppCompatActivity {

        private RecyclerView historyRecyclerView;
        private TextView emptyTextView;

        private BottomAppBar bottomAppBar;
        private FloatingActionButton fabHome;

        private ImageView btnMain, btnHistory, btnFavorites, btnSearch, btnProfile;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_history);

            initViews();
            setupBottomNavigation();
            setFabToHistoryPosition();
            highlightCurrentTab();
            loadWatchHistory();
        }

        private void initViews() {
            historyRecyclerView = findViewById(R.id.historyRecyclerView);
            emptyTextView = findViewById(R.id.emptyTextView);
            bottomAppBar = findViewById(R.id.app_bar);
            fabHome = findViewById(R.id.fab_home);
            btnMain = findViewById(R.id.btn_center);

            btnHistory = findViewById(R.id.btn_history);
            btnFavorites = findViewById(R.id.btn_favorites);
            btnSearch = findViewById(R.id.btn_search);
            btnProfile = findViewById(R.id.btn_profile);
        }

        private void setupBottomNavigation() {
            btnSearch.setOnClickListener(v -> {
                BaseBottomNavigationHelper.setFabPosition(
                        bottomAppBar,
                        fabHome,
                        BaseBottomNavigationHelper.SEARCH_POSITION
                );

                fabHome.postDelayed(() -> {
                    Intent intent = new Intent(this, ExploreActivity.class);
                    startActivity(intent);
                }, 200); // Giảm delay xuống 200ms
            });

            btnFavorites.setOnClickListener(v -> {
                BaseBottomNavigationHelper.setFabPosition(
                        bottomAppBar,
                        fabHome,
                        BaseBottomNavigationHelper.FAVORITES_POSITION
                );

                fabHome.postDelayed(() -> {
                    Intent intent = new Intent(this, FavoriteActivity.class);
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

        private void setFabToHistoryPosition() {
            BaseBottomNavigationHelper.setFabPositionImmediate(
                    bottomAppBar,
                    fabHome,
                    BaseBottomNavigationHelper.HISTORY_POSITION
            );
        }

        private void highlightCurrentTab() {
            int whiteColor = ContextCompat.getColor(this, android.R.color.white);
            int selectedColor = ContextCompat.getColor(this, R.color.selected_tab_color);

            btnHistory.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN);
            btnFavorites.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
            btnSearch.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
            btnProfile.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        }

        private void loadWatchHistory() {
            // Hiển thị thông báo trống nếu chưa có lịch sử xem
            emptyTextView.setText("📺\n\nChưa có lịch sử xem phim nào\n\nHãy bắt đầu xem những bộ phim yêu thích!");
        }
    }
