package com.example.movies_app.Activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movies_app.Adapter.HistoryMoviesAdapter;
import com.example.movies_app.Database.entity.HistoryEntry;
import com.example.movies_app.Database.entity.Movie;
import com.example.movies_app.Helper.BaseBottomNavigationHelper;
import com.example.movies_app.R;
import com.example.movies_app.service.WatchHistoryService;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements HistoryMoviesAdapter.OnHistoryClickListener {

    private RecyclerView historyRecyclerView;
    private TextView emptyTextView;
    private HistoryMoviesAdapter historyAdapter;
    private BottomAppBar bottomAppBar;
    private FloatingActionButton fabHome;
    private ImageView btnMain, btnHistory, btnFavorites, btnSearch, btnProfile;
    private WatchHistoryService watchHistoryService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initViews();
        initServices();
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

        // Setup RecyclerView
        historyRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        historyAdapter = new HistoryMoviesAdapter(this, new ArrayList<>());
        historyAdapter.setOnHistoryClickListener(this);
        historyRecyclerView.setAdapter(historyAdapter);
    }

    private void initServices() {
        watchHistoryService = WatchHistoryService.getInstance(this);
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
            }, 200);
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
        watchHistoryService.getWatchHistory(new WatchHistoryService.WatchHistoryCallback() {
            @Override
            public void onSuccess(List<HistoryEntry> historyEntries) {
                runOnUiThread(() -> {
                    if (historyEntries != null && !historyEntries.isEmpty()) {
                        // Chuyển đổi HistoryEntry thành Movie để hiển thị
                        List<Movie> movies = new ArrayList<>();
                        for (HistoryEntry entry : historyEntries) {
                            Movie movie = entry.movie;
                            // Set watchDate vào lastUpdated để hiển thị
                            movie.setLastUpdated(entry.watchDate);
                            movies.add(movie);
                        }

                        historyAdapter.updateHistory(movies);
                        historyRecyclerView.setVisibility(View.VISIBLE);
                        emptyTextView.setVisibility(View.GONE);
                    } else {
                        showEmptyState();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(HistoryActivity.this, error, Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
            }
        });
    }

    private void showEmptyState() {
        historyRecyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.VISIBLE);
        emptyTextView.setText("📺\n\nChưa có lịch sử xem phim nào\n\nHãy bắt đầu xem những bộ phim yêu thích!");
    }

    @Override
    public void onHistoryItemClick(Movie movie) {
        // Chuyển đến DetailActivity để xem lại phim
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("object", movie);
        startActivity(intent);
    }

    @Override
    public void onRemoveFromHistory(Movie movie) {
        watchHistoryService.removeWatchHistory(movie.getId(), new WatchHistoryService.WatchHistoryOperationCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(HistoryActivity.this, message, Toast.LENGTH_SHORT).show();
                    loadWatchHistory(); // Tải lại danh sách
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(HistoryActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại lịch sử khi quay về activity
        loadWatchHistory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (watchHistoryService != null) {
            watchHistoryService.shutdown();
        }
    }
}