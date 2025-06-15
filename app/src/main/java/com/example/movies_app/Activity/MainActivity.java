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
import com.example.movies_app.Database.entity.FavoriteMovie;
import com.example.movies_app.Database.entity.Movie;
import com.example.movies_app.Database.entity.SearchHistory;
import com.example.movies_app.Database.entity.WatchHistory;
import com.example.movies_app.R;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    // ===== THÊM MỚI: Dynamic Category Titles =====
    private TextView newMoviesTitle, upcomingTitle;

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

                    // ===== SỬ DỤNG APPROACH 3: Phân loại thông minh =====
                    divideMoviesIntoSmartCategories(movies);

                    runOnUiThread(() -> {
                        setupNewMoviesAdapter();
                        setupUpcomingMoviesAdapter();
                        updateCategoryTitles();

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

    // ===== APPROACH 3: SMART CATEGORIZATION =====
    private void divideMoviesIntoSmartCategories(List<Movie> movies) {
        newMovies.clear();
        upcomingMovies.clear();

        try {
            // Lấy preferences của user
            SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
            int currentUserId = prefs.getInt("user_id", -1);

            if (currentUserId != -1) {
                // Phân tích user behavior
                analyzeUserPreferencesAndCategorize(movies, currentUserId);
            } else {
                // Fallback: phân loại theo rating và popularity
                categorizeByRatingAndPopularity(movies);
            }

            // Đảm bảo balance categories
            balanceCategories(movies);

        } catch (Exception e) {
            Log.e("MainActivity", "Error in smart categorization: " + e.getMessage());
            // Fallback to simple division
            fallbackDivision(movies);
        }
    }

    private void analyzeUserPreferencesAndCategorize(List<Movie> movies, int userId) {
        try {
            // Lấy watch history và favorites của user từ database hiện tại
            List<WatchHistory> watchHistoryList = database.movieDao().getWatchHistoryByUser(userId);
            List<FavoriteMovie> favoriteMoviesList = database.movieDao().getFavoriteMoviesByUser(userId);

            // Convert sang list Movie objects
            List<Movie> viewedMovies = getMoviesFromWatchHistory(watchHistoryList);
            List<Movie> favoriteMovies = getMoviesFromFavorites(favoriteMoviesList);

            // Phân tích genre preferences
            Map<String, Double> genreScores = analyzeGenrePreferences(viewedMovies, favoriteMovies);

            // Phân tích rating preferences
            double avgPreferredRating = calculateAveragePreferredRating(viewedMovies, favoriteMovies);

            // Categorize movies based on analysis
            for (Movie movie : movies) {
                double relevanceScore = calculateMovieRelevanceScore(movie, genreScores, avgPreferredRating);

                if (relevanceScore >= 7.0) {
                    newMovies.add(movie); // "Đề xuất cho bạn"
                } else {
                    upcomingMovies.add(movie); // "Khám phá thêm"
                }
            }

            // Sort theo relevance score
            sortMoviesByRelevance(newMovies, genreScores, avgPreferredRating);
            sortMoviesByPopularity(upcomingMovies);

        } catch (Exception e) {
            Log.e("MainActivity", "Error analyzing user preferences: " + e.getMessage());
            categorizeByRatingAndPopularity(movies);
        }
    }

    // Helper methods để convert data từ watch history và favorites
    private List<Movie> getMoviesFromWatchHistory(List<WatchHistory> watchHistoryList) {
        List<Movie> movies = new ArrayList<>();
        if (watchHistoryList != null) {
            for (WatchHistory history : watchHistoryList) {
                try {
                    Movie movie = database.movieDao().getMovieById(history.getMovieId());
                    if (movie != null) {
                        movies.add(movie);
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Error getting movie from watch history: " + e.getMessage());
                }
            }
        }
        return movies;
    }

    private List<Movie> getMoviesFromFavorites(List<FavoriteMovie> favoriteMoviesList) {
        List<Movie> movies = new ArrayList<>();
        if (favoriteMoviesList != null) {
            for (FavoriteMovie favorite : favoriteMoviesList) {
                try {
                    Movie movie = database.movieDao().getMovieById(favorite.getMovieId());
                    if (movie != null) {
                        movies.add(movie);
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Error getting movie from favorites: " + e.getMessage());
                }
            }
        }
        return movies;
    }

    private Map<String, Double> analyzeGenrePreferences(List<Movie> viewedMovies, List<Movie> favoriteMovies) {
        Map<String, Double> genreScores = new HashMap<>();

        // Phân tích từ favorite movies (weight = 2.0)
        if (favoriteMovies != null) {
            for (Movie movie : favoriteMovies) {
                String genreString = movie.getGenres(); // Sử dụng getGenres() thay vì getGenre()
                if (genreString != null && !genreString.isEmpty()) {
                    String[] genres = genreString.split(",");
                    for (String genre : genres) {
                        genre = genre.trim().toLowerCase();
                        genreScores.put(genre, genreScores.getOrDefault(genre, 0.0) + 2.0);
                    }
                }
            }
        }

        // Phân tích từ viewed movies (weight = 1.0)
        if (viewedMovies != null) {
            for (Movie movie : viewedMovies) {
                String genreString = movie.getGenres();
                if (genreString != null && !genreString.isEmpty()) {
                    String[] genres = genreString.split(",");
                    for (String genre : genres) {
                        genre = genre.trim().toLowerCase();
                        genreScores.put(genre, genreScores.getOrDefault(genre, 0.0) + 1.0);
                    }
                }
            }
        }

        return genreScores;
    }

    private double calculateAveragePreferredRating(List<Movie> viewedMovies, List<Movie> favoriteMovies) {
        double totalRating = 0.0;
        int count = 0;

        // Ưu tiên rating từ favorite movies
        if (favoriteMovies != null && !favoriteMovies.isEmpty()) {
            for (Movie movie : favoriteMovies) {
                try {
                    // Chuyển đổi imdbRating string thành double
                    double rating = parseRating(movie.getImdbRating());
                    if (rating > 0) {
                        totalRating += rating * 2; // Weight x2 for favorites
                        count += 2;
                    }
                } catch (Exception e) {
                    Log.w("MainActivity", "Error parsing rating for favorite movie: " + e.getMessage());
                }
            }
        }

        // Thêm rating từ viewed movies
        if (viewedMovies != null && !viewedMovies.isEmpty()) {
            for (Movie movie : viewedMovies) {
                try {
                    double rating = parseRating(movie.getImdbRating());
                    if (rating > 0) {
                        totalRating += rating;
                        count++;
                    }
                } catch (Exception e) {
                    Log.w("MainActivity", "Error parsing rating for viewed movie: " + e.getMessage());
                }
            }
        }

        return count > 0 ? totalRating / count : 7.0; // Default 7.0 nếu không có data
    }

    // Helper method để parse rating string thành double
    private double parseRating(String ratingString) {
        if (ratingString == null || ratingString.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(ratingString);
        } catch (NumberFormatException e) {
            Log.w("MainActivity", "Cannot parse rating: " + ratingString);
            return 0.0;
        }
    }

    private double calculateMovieRelevanceScore(Movie movie, Map<String, Double> genreScores, double avgPreferredRating) {
        double movieRating = parseRating(movie.getImdbRating());
        double score = movieRating > 0 ? movieRating : 6.0; // Base score

        // Bonus điểm cho genre preference
        String genreString = movie.getGenres();
        if (genreString != null && !genreString.isEmpty()) {
            String[] genres = genreString.toLowerCase().split(",");
            double genreBonus = 0.0;

            for (String genre : genres) {
                genre = genre.trim();
                if (genreScores.containsKey(genre)) {
                    genreBonus += genreScores.get(genre) * 0.5; // 0.5 điểm per genre match
                }
            }

            score += Math.min(genreBonus, 3.0); // Max 3 điểm bonus cho genre
        }

        // Bonus điểm nếu rating gần với preference
        if (movieRating > 0) {
            double ratingDiff = Math.abs(movieRating - avgPreferredRating);
            if (ratingDiff <= 1.0) {
                score += (1.0 - ratingDiff); // Càng gần càng nhiều điểm
            }
        }

        // Bonus điểm cho phim có view count cao (trending)
        if (movie.getViewCount() > 100) { // Giảm threshold xuống 100
            score += 0.5;
        }

        return score;
    }

    private void categorizeByRatingAndPopularity(List<Movie> movies) {
        // Backup method khi không có user data
        List<Movie> sortedMovies = new ArrayList<>(movies);

        // Sort theo rating giảm dần
        sortedMovies.sort((m1, m2) -> {
            double rating1 = parseRating(m1.getImdbRating());
            double rating2 = parseRating(m2.getImdbRating());
            return Double.compare(rating2, rating1);
        });

        int highRatedCount = 0;
        for (Movie movie : sortedMovies) {
            double rating = parseRating(movie.getImdbRating());
            if (rating >= 7.5 && highRatedCount < movies.size() * 0.6) {
                newMovies.add(movie);
                highRatedCount++;
            } else {
                upcomingMovies.add(movie);
            }
        }
    }

    private void sortMoviesByRelevance(List<Movie> movies, Map<String, Double> genreScores, double avgPreferredRating) {
        movies.sort((m1, m2) -> {
            double score1 = calculateMovieRelevanceScore(m1, genreScores, avgPreferredRating);
            double score2 = calculateMovieRelevanceScore(m2, genreScores, avgPreferredRating);
            return Double.compare(score2, score1); // Descending order
        });
    }

    private void sortMoviesByPopularity(List<Movie> movies) {
        movies.sort((m1, m2) -> {
            // Sort by view count, then rating
            int viewCompare = Integer.compare(m2.getViewCount(), m1.getViewCount());
            if (viewCompare != 0) return viewCompare;

            double rating1 = parseRating(m1.getImdbRating());
            double rating2 = parseRating(m2.getImdbRating());
            return Double.compare(rating2, rating1);
        });
    }

    private void balanceCategories(List<Movie> allMovies) {
        // Đảm bảo mỗi category có ít nhất 3 phim
        if (newMovies.size() < 3 && upcomingMovies.size() > 6) {
            // Move some movies from upcoming to new
            List<Movie> movedMovies = new ArrayList<>(upcomingMovies.subList(0, Math.min(3, upcomingMovies.size())));
            newMovies.addAll(movedMovies);
            upcomingMovies.removeAll(movedMovies);
        } else if (upcomingMovies.size() < 3 && newMovies.size() > 6) {
            // Move some movies from new to upcoming
            List<Movie> movedMovies = new ArrayList<>(newMovies.subList(newMovies.size() - 3, newMovies.size()));
            upcomingMovies.addAll(0, movedMovies);
            newMovies.removeAll(movedMovies);
        }

        // Giới hạn số lượng để tránh lag
        if (newMovies.size() > 20) {
            newMovies = new ArrayList<>(newMovies.subList(0, 20));
        }
        if (upcomingMovies.size() > 20) {
            upcomingMovies = new ArrayList<>(upcomingMovies.subList(0, 20));
        }
    }

    private void fallbackDivision(List<Movie> movies) {
        // Original simple division as fallback
        newMovies.clear();
        upcomingMovies.clear();

        int halfSize = movies.size() / 2;
        for (int i = 0; i < movies.size(); i++) {
            if (i < halfSize) {
                newMovies.add(movies.get(i));
            } else {
                upcomingMovies.add(movies.get(i));
            }
        }
    }

    // ===== DYNAMIC CATEGORY TITLES =====
    private void updateCategoryTitles() {
        // Uncomment nếu bạn có TextView cho titles trong layout
        /*
        if (newMoviesTitle != null && upcomingTitle != null) {
            SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
            int currentUserId = prefs.getInt("user_id", -1);

            if (currentUserId != -1) {
                newMoviesTitle.setText("⭐ Đề xuất cho bạn");
                upcomingTitle.setText("🔍 Khám phá thêm");
            } else {
                newMoviesTitle.setText("🎬 Phim chất lượng cao");
                upcomingTitle.setText("🎯 Phim thịnh hành");
            }
        }
        */

        // Log để debug
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        int currentUserId = prefs.getInt("user_id", -1);

        if (currentUserId != -1) {
            Log.d("MainActivity", "Smart categorization applied for user: " + currentUserId);
            Log.d("MainActivity", "Recommended movies: " + newMovies.size() + ", Explore movies: " + upcomingMovies.size());
        } else {
            Log.d("MainActivity", "Fallback categorization applied (no user login)");
        }
    }

    // ===== GIỮ NGUYÊN CÁC METHOD CŨ =====
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