package com.example.movies_app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movies_app.Adapter.HorizontalGridMovieAdapter;
import com.example.movies_app.Database.AppDatabase;
import com.example.movies_app.Database.entity.Movie;
import com.example.movies_app.Database.entity.SearchHistory;
import com.example.movies_app.R;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    // SỬ DỤNG ADAPTER MỚI
    private HorizontalGridMovieAdapter adapterNewMovies, adapterUpComing, adapterSearchResults;
    private RecyclerView recyclerViewNewMovies, recyclerViewUpComing, homeSearchRecyclerView;
    private ProgressBar loading1, loading2, homeSearchProgressBar;

    // Database và Executor
    private AppDatabase database;
    private ExecutorService executorService;

    // Danh sách phim local
    private List<Movie> allLocalMovies;
    private List<Movie> newMovies;
    private List<Movie> upcomingMovies;
    // Search components
    private EditText homeSearchEditText;
    private LinearLayout homeSearchResultsContainer, homeMainContent;
    private TextView homeResultsCountTxt;
    private Drawable closeIcon;

    // Bottom Navigation Components
    private BottomAppBar bottomAppBar;
    private ImageView btnMain, btnHistory, btnFavorites, btnSearch, btnProfile;
    private FloatingActionButton fabHome;
    private Handler searchHandler;
    private Runnable searchRunnable;
    private static final int SEARCH_DELAY = 300;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchHandler = new Handler(Looper.getMainLooper());

        initializeDatabase();
        initViews();
        setupBottomNavigation();
        setupSearchListeners();
        loadMoviesFromDatabase();
        highlightHomeTab();

        closeIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_close, null);
        if (closeIcon != null) {
            closeIcon.setBounds(0, 0, closeIcon.getIntrinsicWidth(), closeIcon.getIntrinsicHeight());
        }
        hideCloseIcon();
    }

    private void initializeDatabase() {
        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        // Khởi tạo danh sách
        allLocalMovies = new ArrayList<>();
        newMovies = new ArrayList<>();
        upcomingMovies = new ArrayList<>();

        new Thread(() -> {
            try {
                database.userDao().getAllUsers();
                Log.d("Database", "Database initialized successfully");
            } catch (Exception e) {
                Log.e("Database", "Error initializing database: " + e.getMessage());
            }
        }).start();
    }

    private void initViews() {
        bottomAppBar = findViewById(R.id.app_bar);

        // Bottom navigation views
        btnHistory = findViewById(R.id.btn_history);
        btnFavorites = findViewById(R.id.btn_favorites);
        btnSearch = findViewById(R.id.btn_search);
        btnProfile = findViewById(R.id.btn_profile);
        btnMain = findViewById(R.id.btn_center);
        fabHome = findViewById(R.id.fab_home);

        // Content views
        recyclerViewNewMovies = findViewById(R.id.view1);
        // THAY ĐỔI: Sử dụng LinearLayoutManager với orientation HORIZONTAL
        recyclerViewNewMovies.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        recyclerViewUpComing = findViewById(R.id.view2);
        recyclerViewUpComing.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        loading1 = findViewById(R.id.loading1);
        loading2 = findViewById(R.id.loading2);

        // Search views
        homeSearchEditText = findViewById(R.id.homeSearchEditText);
        homeSearchRecyclerView = findViewById(R.id.homeSearchRecyclerView);
        homeSearchResultsContainer = findViewById(R.id.homeSearchResultsContainer);
        homeMainContent = findViewById(R.id.homeMainContent);
        homeResultsCountTxt = findViewById(R.id.homeResultsCountTxt);
        homeSearchProgressBar = findViewById(R.id.homeSearchProgressBar);

        // Setup search RecyclerView với GridLayoutManager
        homeSearchRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private void loadMoviesFromDatabase() {
        loading1.setVisibility(View.VISIBLE);
        loading2.setVisibility(View.VISIBLE);

        executorService.execute(() -> {
            try {
                // Lấy tất cả phim từ database
                List<Movie> movies = database.movieDao().getAllMovies();

                if (movies != null && !movies.isEmpty()) {
                    allLocalMovies.clear();
                    allLocalMovies.addAll(movies);

                    // Chia phim thành 2 nhóm
                    divideMoviesIntoCategories(movies);

                    runOnUiThread(() -> {
                        setupNewMoviesAdapter();
                        setupUpcomingMoviesAdapter();

                        loading1.setVisibility(View.GONE);
                        loading2.setVisibility(View.GONE);
                    });
                } else {
                    runOnUiThread(() -> {
                        loading1.setVisibility(View.GONE);
                        loading2.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Không có phim nào trong database", Toast.LENGTH_SHORT).show();
                    });
                }

            } catch (Exception e) {
                Log.e("MainActivity", "Error loading movies from database: " + e.getMessage());
                runOnUiThread(() -> {
                    loading1.setVisibility(View.GONE);
                    loading2.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Lỗi khi tải dữ liệu phim", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void divideMoviesIntoCategories(List<Movie> movies) {
        newMovies.clear();
        upcomingMovies.clear();

        // Chia đôi danh sách phim
        int halfSize = movies.size() / 2;

        for (int i = 0; i < movies.size(); i++) {
            if (i < halfSize) {
                newMovies.add(movies.get(i));
            } else {
                upcomingMovies.add(movies.get(i));
            }
        }
    }

    private void setupNewMoviesAdapter() {
        adapterNewMovies = new HorizontalGridMovieAdapter(this, newMovies);
        recyclerViewNewMovies.setAdapter(adapterNewMovies);
    }

    private void setupUpcomingMoviesAdapter() {
        adapterUpComing = new HorizontalGridMovieAdapter(this, upcomingMovies);
        recyclerViewUpComing.setAdapter(adapterUpComing);
    }

    private void setupBottomNavigation() {
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        btnFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
            startActivity(intent);
        });

        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ExploreActivity.class);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        fabHome.setOnClickListener(v -> {
            bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
            fabHome.postDelayed(() -> {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }, 100);
        });
    }

    private void setupSearchListeners() {
        // ===== THAY THẾ PHẦN setupSearchListeners CŨ =====

        // Realtime search với TextWatcher
        homeSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Hiển thị/ẩn close icon
                if (s.length() > 0) {
                    showCloseIcon();

                    // Cancel previous search
                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }

                    // Schedule new search with delay
                    searchRunnable = () -> performRealtimeSearch(s.toString().trim());
                    searchHandler.postDelayed(searchRunnable, SEARCH_DELAY);

                } else {
                    hideCloseIcon();
                    // Clear search when text is empty
                    clearSearchResults();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Giữ nguyên touch listener cho icon clicks
        homeSearchEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() != MotionEvent.ACTION_UP) {
                return false;
            }

            Drawable drawableLeft = homeSearchEditText.getCompoundDrawables()[0];
            if (drawableLeft != null && event.getRawX() <= (homeSearchEditText.getLeft()
                    + drawableLeft.getBounds().width() + homeSearchEditText.getPaddingStart())) {
                // Force search immediately when clicking search icon
                String query = homeSearchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    performRealtimeSearch(query);
                }
                return true;
            }

            Drawable drawableRight = homeSearchEditText.getCompoundDrawables()[2];
            if (drawableRight != null && event.getRawX() >= (homeSearchEditText.getRight()
                    - drawableRight.getBounds().width() - homeSearchEditText.getPaddingEnd())) {
                clearSearch();
                return true;
            }

            return false;
        });

        // Enter key để force search ngay lập tức
        homeSearchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = homeSearchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    performRealtimeSearch(query);
                }
                return true;
            }
            return false;
        });
    }

    private void showCloseIcon() {
        Drawable[] drawables = homeSearchEditText.getCompoundDrawables();
        homeSearchEditText.setCompoundDrawables(
                drawables[0], drawables[1], closeIcon, drawables[3]
        );
    }

    private void hideCloseIcon() {
        Drawable[] drawables = homeSearchEditText.getCompoundDrawables();
        homeSearchEditText.setCompoundDrawables(
                drawables[0], drawables[1], null, drawables[3]
        );
    }

    private void performRealtimeSearch(String query) {
        if (query.isEmpty()) {
            clearSearchResults();
            return;
        }

        // Cancel previous search nếu có
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }

        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(homeSearchEditText.getWindowToken(), 0);

        // Show search results container
        showSearchResults();

        // Show loading for longer searches
        if (query.length() >= 2) {
            homeSearchProgressBar.setVisibility(View.VISIBLE);
            homeResultsCountTxt.setText("🔍 Đang tìm kiếm \"" + query + "\"...");
        }

        // Perform search in background
        executorService.execute(() -> {
            try {
                List<Movie> searchResults = database.movieDao().searchMovies("%" + query + "%");

                runOnUiThread(() -> {
                    homeSearchProgressBar.setVisibility(View.GONE);

                    if (searchResults != null && !searchResults.isEmpty()) {
                        int resultCount = searchResults.size();
                        homeResultsCountTxt.setText("🔍 Tìm thấy " + resultCount + " kết quả cho \"" + query + "\"");

                        adapterSearchResults = new HorizontalGridMovieAdapter(MainActivity.this, searchResults);
                        homeSearchRecyclerView.setAdapter(adapterSearchResults);
                    } else {
                        homeResultsCountTxt.setText("❌ Không tìm thấy kết quả cho \"" + query + "\"");
                        // Clear adapter
                        homeSearchRecyclerView.setAdapter(null);
                    }
                });

            } catch (Exception e) {
                Log.e("MainActivity", "Error in realtime search: " + e.getMessage());
                runOnUiThread(() -> {
                    homeSearchProgressBar.setVisibility(View.GONE);
                    homeResultsCountTxt.setText("⚠️ Đã xảy ra lỗi khi tìm kiếm");
                });
            }
        });

        // Save search history for queries longer than 2 characters
        if (query.length() >= 3) {
            saveSearchHistory(query);
        }
    }
    private void clearSearchResults() {
        homeSearchResultsContainer.setVisibility(View.GONE);
        homeMainContent.setVisibility(View.VISIBLE);
        homeSearchRecyclerView.setAdapter(null);
    }

    private void saveSearchHistory(String query) {
        // Save search history in background
        executorService.execute(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
                int currentUserId = prefs.getInt("user_id", -1);

                if (currentUserId != -1) {
                    SearchHistory searchHistory = new SearchHistory(
                            currentUserId,
                            query,
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())
                    );
                    database.movieDao().insertSearchHistory(searchHistory);
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Error saving search history: " + e.getMessage());
            }
        });
    }
    private void showSearchResults() {
        homeMainContent.setVisibility(View.GONE);
        homeSearchResultsContainer.setVisibility(View.VISIBLE);
    }

    private void clearSearch() {
        homeSearchEditText.setText("");
        homeSearchResultsContainer.setVisibility(View.GONE);
        homeMainContent.setVisibility(View.VISIBLE);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(homeSearchEditText.getWindowToken(), 0);
        homeSearchEditText.clearFocus();
    }

    private void highlightHomeTab() {
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);

        btnHistory.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnFavorites.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnSearch.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnProfile.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);

        fabHome.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.selected_tab_color));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}