package com.example.movies_app.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movies_app.Adapter.FavoriteMoviesAdapter;
import com.example.movies_app.Database.AppDatabase;
import com.example.movies_app.Database.dao.MovieDao;
import com.example.movies_app.Database.entity.Movie;
import com.example.movies_app.Helper.BaseBottomNavigationHelper;
import com.example.movies_app.R;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoriteActivity extends AppCompatActivity implements FavoriteMoviesAdapter.OnFavoriteMovieClickListener {

    private RecyclerView favoriteRecyclerView;
    private FavoriteMoviesAdapter favoriteAdapter;
    private LinearLayout emptyLayout;
    private MovieDao movieDao;
    private ExecutorService executorService;

    // Bottom Navigation Components
    private ImageView btnMain, btnHistory, btnFavorites, btnSearch, btnProfile;
    private FloatingActionButton fabHome;
    private BottomAppBar bottomAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        favoriteRecyclerView = findViewById(R.id.favoriteRecyclerView);
        emptyLayout = findViewById(R.id.emptyLayout);
        initDatabase();
        initViews();
        favoriteRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        favoriteAdapter = new FavoriteMoviesAdapter(this, new ArrayList<>());
        favoriteAdapter.setOnFavoriteMovieClickListener(this);
        favoriteRecyclerView.setAdapter(favoriteAdapter);
        AppDatabase db = AppDatabase.getInstance(this);
        movieDao = db.movieDao();
        executorService = Executors.newFixedThreadPool(2);
        setupBottomNavigation();
        highlightCurrentTab();
        loadFavoriteMovies();

        setFabToFavoritePosition();
    }

    private void initDatabase() {
        AppDatabase database = AppDatabase.getInstance(this);
        movieDao = database.movieDao();
        executorService = Executors.newFixedThreadPool(2);
    }

    private void initViews() {
        favoriteRecyclerView = findViewById(R.id.favoriteRecyclerView);
        favoriteRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Khởi tạo adapter với danh sách rỗng
        favoriteAdapter = new FavoriteMoviesAdapter(this, new ArrayList<>());
        favoriteAdapter.setOnFavoriteMovieClickListener(this);
        favoriteRecyclerView.setAdapter(favoriteAdapter);

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
            }, 200);
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
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        int selectedColor = ContextCompat.getColor(this, R.color.selected_tab_color);

        btnHistory.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnFavorites.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN);
        btnSearch.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnProfile.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
    }

    private void loadFavoriteMovies() {
        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        int currentUserId = prefs.getInt("user_id", -1);

        if (currentUserId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem phim yêu thích", Toast.LENGTH_SHORT).show();
            return;
        }

        // Thực hiện truy vấn trong background thread
        executorService.execute(() -> {
            try {
                List<Movie> favoriteMovies = movieDao.getFavoriteMoviesWithDetailsByUser(currentUserId);

                // Cập nhật UI trong main thread
                runOnUiThread(() -> {
                    if (favoriteMovies != null && !favoriteMovies.isEmpty()) {
                        favoriteAdapter.updateFavoriteMovies(favoriteMovies);
                    } else {
                        Toast.makeText(this, "Chưa có phim yêu thích nào", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi tải danh sách yêu thích: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onFavoriteMovieClick(Movie movie) {
        Intent i = new Intent(FavoriteActivity.this, DetailActivity.class);
        i.putExtra("id", movie.getId());
        startActivity(i);
    }

    @Override
    public void onRemoveFromFavorites(Movie movie) {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        executorService.execute(() -> {
            movieDao.deleteFavoriteMovieByIds(movie.getId(), userId);
            runOnUiThread(() -> {
                Toast.makeText(FavoriteActivity.this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                loadFavoriteMovies();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại danh sách khi quay lại activity
        loadFavoriteMovies();
    }
}