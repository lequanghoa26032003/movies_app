package com.example.movies_app.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movies_app.Adapter.HistoryMoviesAdapter;
import com.example.movies_app.Database.AppDatabase;
import com.example.movies_app.Database.entity.HistoryEntry;
import com.example.movies_app.Database.entity.Movie;
import com.example.movies_app.Helper.BaseBottomNavigationHelper;
import com.example.movies_app.R;
import com.example.movies_app.service.WatchHistoryService;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private TextView emptyTextView;
    private BottomAppBar bottomAppBar;
    private FloatingActionButton fabHome;
    private ImageView btnMain, btnHistory, btnFavorites, btnSearch, btnProfile;

    private HistoryMoviesAdapter historyAdapter;
    private WatchHistoryService watchHistoryService;
    private AppDatabase database;
    private ExecutorService executorService;
    private int currentUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initServices();
        getCurrentUser();
        initViews();
        setupBottomNavigation();
        setFabToHistoryPosition();
        highlightCurrentTab();
        loadWatchHistory();
    }

    private void initServices() {
        watchHistoryService = WatchHistoryService.getInstance(this);
        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
    }

    private void getCurrentUser() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);
        Log.d("HistoryActivity", "Current user ID: " + currentUserId);
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

        historyRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Initialize adapter with empty list
        historyAdapter = new HistoryMoviesAdapter(this, null);
        historyRecyclerView.setAdapter(historyAdapter);

        // Set up adapter listeners
        historyAdapter.setOnHistoryClickListener(new HistoryMoviesAdapter.OnHistoryClickListener() {
            @Override
            public void onHistoryItemClick(Movie movie) {
                openMovieDetail(movie);
            }

            @Override
            public void onRemoveFromHistory(Movie movie) {
                removeFromHistory(movie);
            }
        });
    }

    private void openMovieDetail(Movie movie) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("object", movie);
        startActivity(intent);
    }

    private void removeFromHistory(Movie movie) {
        if (currentUserId == -1 || movie == null) return;

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa khỏi lịch sử")
                .setMessage("Bạn có chắc muốn xóa \"" + movie.getTitle() + "\" khỏi lịch sử xem?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    executorService.execute(() -> {
                        try {
                            database.watchHistoryDao().deleteWatchHistoryByUserAndMovie(currentUserId, movie.getId());
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Đã xóa khỏi lịch sử", Toast.LENGTH_SHORT).show();
                                loadWatchHistory(); // Refresh list
                            });
                        } catch (Exception e) {
                            Log.e("HistoryActivity", "Error removing from history: " + e.getMessage());
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Lỗi khi xóa khỏi lịch sử", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadWatchHistory() {
        if (currentUserId == -1) {
            showEmptyState("Vui lòng đăng nhập để xem lịch sử");
            return;
        }

        watchHistoryService.getWatchHistory(new WatchHistoryService.WatchHistoryCallback() {
            @Override
            public void onSuccess(List<HistoryEntry> historyEntries) {
                runOnUiThread(() -> {
                    if (historyEntries == null || historyEntries.isEmpty()) {
                        showEmptyState("Chưa có lịch sử xem phim nào");
                    } else {
                        showHistoryList(historyEntries);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e("HistoryActivity", "Error loading history: " + error);
                    showEmptyState("Lỗi tải lịch sử: " + error);
                });
            }
        });
    }

    private void showEmptyState(String message) {
        historyRecyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.VISIBLE);
        emptyTextView.setText(message);
    }

    private void showHistoryList(List<HistoryEntry> historyEntries) {
        historyRecyclerView.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);

        // Convert HistoryEntry to Movie list for adapter
        List<Movie> movieList = new ArrayList<>(); // IMPORT ĐÃ ĐƯỢC THÊM
        for (HistoryEntry entry : historyEntries) {
            if (entry.movie != null) {
                // Set last updated to watch date for display
                entry.movie.setLastUpdated(entry.watchDate);
                movieList.add(entry.movie);
            }
        }

        historyAdapter.updateHistory(movieList);
    }

    private void setupBottomNavigation() {
        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExploreActivity.class);
            startActivity(intent);
        });

        btnFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(this, FavoriteActivity.class);
            startActivity(intent);
        });

        btnMain.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
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
        btnMain.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnFavorites.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnSearch.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnProfile.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh history when returning to this activity
        loadWatchHistory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}