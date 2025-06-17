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
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movies_app.Adapter.FilmListAdapter;
import com.example.movies_app.Database.AppDatabase;
import com.example.movies_app.Database.entity.Movie;
import com.example.movies_app.Database.entity.SearchHistory;
import com.example.movies_app.Domain.ListFilm;
import com.example.movies_app.Helper.BaseBottomNavigationHelper;
import com.example.movies_app.Helper.FilterManager;
import com.example.movies_app.Helper.GenreHelper;
import com.example.movies_app.R;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ExploreActivity extends AppCompatActivity {

    private AppDatabase database;
    private FilmListAdapter trendingAdapter, categoryAdapter;

    // Search & Filter Components
    private EditText searchEditText;
    private RecyclerView exploreRecyclerView, categoryRecyclerView, trendingRecyclerView;
    private RecyclerView.Adapter adapterSearchResults;
    private TextView resultsCountTxt;
    private ProgressBar searchProgressBar;
    private LinearLayout searchResultsContainer;
    private ImageView filterBtn;
    private FilterManager filterManager;
    private Drawable closeIcon;
    private BottomAppBar bottomAppBar;

    private LinearLayout trendingSection, categorySection;

    private ImageView btnMain, btnHistory, btnFavorites, btnSearch, btnProfile;
    private FloatingActionButton fabHome;

    private Handler searchHandler;
    private Runnable searchRunnable;
    private static final int SEARCH_DELAY = 400;
    private boolean isSearching = false;
    private boolean isUpdatingText = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        searchHandler = new Handler(Looper.getMainLooper());
        database = AppDatabase.getInstance(this);
        filterManager = new FilterManager(this);

        initViews();
        setupBottomNavigation();
        highlightCurrentTab();
        setupSearchListeners();
        focusSearchBox();

        // ✅ SỬ DỤNG PHƯƠNG PHÁP PHÂN CHIA THÔNG MINH MỚI
        loadSmartTrendingMovies();
        loadSmartPopularCategories();

        setFabToExplorePosition();

        closeIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_close, null);
        if (closeIcon != null) {
            closeIcon.setBounds(0, 0, closeIcon.getIntrinsicWidth(), closeIcon.getIntrinsicHeight());
        }
        hideCloseIcon();
    }

    private void initViews() {
        // Search components
        searchEditText = findViewById(R.id.exploreSearchEditText);
        exploreRecyclerView = findViewById(R.id.exploreRecyclerView);
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        trendingRecyclerView = findViewById(R.id.trendingRecyclerView);
        resultsCountTxt = findViewById(R.id.exploreResultsCountTxt);
        searchProgressBar = findViewById(R.id.exploreSearchProgressBar);
        searchResultsContainer = findViewById(R.id.exploreSearchResultsContainer);
        filterBtn = findViewById(R.id.exploreFilterBtn);

        // Content sections
        bottomAppBar = findViewById(R.id.app_bar);
        trendingSection = findViewById(R.id.trendingSection);
        categorySection = findViewById(R.id.categorySection);

        // Setup RecyclerViews
        exploreRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        trendingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Filter button
        filterBtn.setOnClickListener(v -> showFilterDialog());

        // Bottom Navigation Views
        btnHistory = findViewById(R.id.btn_history);
        btnFavorites = findViewById(R.id.btn_favorites);
        btnSearch = findViewById(R.id.btn_search);
        btnProfile = findViewById(R.id.btn_profile);
        fabHome = findViewById(R.id.fab_home);
        btnMain = findViewById(R.id.btn_center);
    }

    // ✅ PHƯƠNG PHÁP PHÂN CHIA THÔNG MINH MỚI - PHIM THỊNH HÀNH
    private void loadSmartTrendingMovies() {
        new Thread(() -> {
            try {
                List<Movie> allMovies = database.movieDao().getAllMovies();

                if (allMovies != null && !allMovies.isEmpty()) {
                    List<MovieScore> movieScores = calculateTrendingScores(allMovies);

                    // Lấy top 15 phim có điểm cao nhất
                    List<Movie> trendingMovies = new ArrayList<>();
                    int count = Math.min(15, movieScores.size());

                    for (int i = 0; i < count; i++) {
                        trendingMovies.add(movieScores.get(i).movie);
                    }

                    ListFilm listFilm = convertMoviesToListFilm(trendingMovies);

                    runOnUiThread(() -> {
                        displayTrendingMovies(listFilm);
                        Log.d("ExploreActivity", "✅ Loaded " + trendingMovies.size() + " smart trending movies");
                    });
                } else {
                    runOnUiThread(() -> {
                        Log.d("ExploreActivity", "No movies found in database");
                        Toast.makeText(this, "Chưa có phim trong cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e("ExploreActivity", "Error loading smart trending movies: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi tải phim thịnh hành thông minh", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // ✅ PHƯƠNG PHÁP PHÂN CHIA THÔNG MINH MỚI - THỂ LOẠI PHỔ BIẾN
    private void loadSmartPopularCategories() {
        new Thread(() -> {
            try {
                List<Movie> allMovies = database.movieDao().getAllMovies();

                if (allMovies != null && !allMovies.isEmpty()) {
                    List<Movie> popularMovies = getPopularMoviesByGenre(allMovies);
                    ListFilm listFilm = convertMoviesToListFilm(popularMovies);

                    runOnUiThread(() -> {
                        displayCategories(listFilm);
                        Log.d("ExploreActivity", "✅ Loaded " + popularMovies.size() + " smart popular movies by genre");
                    });
                } else {
                    runOnUiThread(() -> {
                        Log.d("ExploreActivity", "No popular movies found");
                    });
                }
            } catch (Exception e) {
                Log.e("ExploreActivity", "Error loading smart popular categories: " + e.getMessage());
            }
        }).start();
    }

    // ✅ THUẬT TOÁN TÍNH ĐIỂM CHO PHIM THỊNH HÀNH
    private List<MovieScore> calculateTrendingScores(List<Movie> movies) {
        List<MovieScore> movieScores = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        for (Movie movie : movies) {
            double score = 0;

            // 1. ĐIỂM RATING (Trọng số 40%)
            try {
                if (movie.getImdbRating() != null && !movie.getImdbRating().isEmpty()) {
                    double rating = Double.parseDouble(movie.getImdbRating());
                    score += rating * 0.4; // Max 4.0 điểm
                }
            } catch (NumberFormatException e) {
                // Nếu không có rating, cho 0 điểm phần này
            }

            // 2. ĐIỂM NĂM PHÁT HÀNH (Trọng số 30%)
            try {
                if (movie.getYear() != null && !movie.getYear().isEmpty()) {
                    int year = Integer.parseInt(movie.getYear());

                    if (year >= 2022) {
                        score += 3.0 * 0.3; // Phim mới nhất: 0.9 điểm
                    } else if (year >= 2018) {
                        score += 2.5 * 0.3; // Phim gần đây: 0.75 điểm
                    } else if (year >= 2010) {
                        score += 2.0 * 0.3; // Phim cũ hơn: 0.6 điểm
                    } else {
                        score += 1.0 * 0.3; // Phim cũ: 0.3 điểm
                    }
                }
            } catch (NumberFormatException e) {
                score += 0.5 * 0.3; // Không xác định năm: 0.15 điểm
            }

            // 3. ĐIỂM THỂ LOẠI HOT (Trọng số 30%)
            String[] hotGenres = {"Action", "Adventure", "Sci-Fi", "Thriller", "Comedy", "Drama"};
            if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
                String movieGenres = movie.getGenres().toLowerCase();
                for (String hotGenre : hotGenres) {
                    if (movieGenres.contains(hotGenre.toLowerCase())) {
                        score += 1.0 * 0.3; // 0.3 điểm cho mỗi thể loại hot
                        break; // Chỉ cộng 1 lần cho mỗi phim
                    }
                }
            }

            movieScores.add(new MovieScore(movie, score));
        }

        // Sắp xếp theo điểm giảm dần
        Collections.sort(movieScores, (ms1, ms2) -> Double.compare(ms2.score, ms1.score));

        return movieScores;
    }

    // ✅ LẤY PHIM PHỔ BIẾN THEO THỂ LOẠI
    private List<Movie> getPopularMoviesByGenre(List<Movie> allMovies) {
        List<Movie> popularMovies = new ArrayList<>();

        // Các thể loại phổ biến nhất
        String[] popularGenres = {"Action", "Comedy", "Drama", "Adventure", "Sci-Fi", "Thriller", "Romance"};

        for (String genre : popularGenres) {
            List<Movie> genreMovies = new ArrayList<>();

            // Tìm phim thuộc thể loại này
            for (Movie movie : allMovies) {
                if (movie.getGenres() != null &&
                        movie.getGenres().toLowerCase().contains(genre.toLowerCase())) {
                    genreMovies.add(movie);
                }
            }

            // Sắp xếp theo rating
            Collections.sort(genreMovies, (m1, m2) -> {
                try {
                    double rating1 = Double.parseDouble(m1.getImdbRating() != null ? m1.getImdbRating() : "0");
                    double rating2 = Double.parseDouble(m2.getImdbRating() != null ? m2.getImdbRating() : "0");
                    return Double.compare(rating2, rating1); // Giảm dần
                } catch (NumberFormatException e) {
                    return 0;
                }
            });

            // Lấy top 2 phim hay nhất của thể loại này
            int count = Math.min(2, genreMovies.size());
            for (int i = 0; i < count; i++) {
                if (!popularMovies.contains(genreMovies.get(i))) {
                    popularMovies.add(genreMovies.get(i));
                }
            }

            // Giới hạn tổng số phim
            if (popularMovies.size() >= 15) {
                break;
            }
        }

        return popularMovies;
    }

    // ✅ CLASS HELPER CHO ĐIỂM PHIM
    private static class MovieScore {
        Movie movie;
        double score;

        MovieScore(Movie movie, double score) {
            this.movie = movie;
            this.score = score;
        }
    }

    // ✅ HIỂN THỊ TRENDING MOVIES
    private void displayTrendingMovies(ListFilm items) {
        if (items != null && items.getData() != null && !items.getData().isEmpty()) {
            trendingAdapter = new FilmListAdapter(items);
            trendingRecyclerView.setAdapter(trendingAdapter);
            Log.d("ExploreActivity", "Trending adapter set with " + items.getData().size() + " items");
        } else {
            Log.d("ExploreActivity", "No trending data to display");
        }
    }

    // ✅ HIỂN THỊ CATEGORIES
    private void displayCategories(ListFilm items) {
        if (items != null && items.getData() != null && !items.getData().isEmpty()) {
            categoryAdapter = new FilmListAdapter(items);
            categoryRecyclerView.setAdapter(categoryAdapter);
            Log.d("ExploreActivity", "Category adapter set with " + items.getData().size() + " items");
        } else {
            Log.d("ExploreActivity", "No category data to display");
        }
    }

    private ListFilm convertMoviesToListFilm(List<Movie> movies) {
        ListFilm listFilm = new ListFilm();
        // Không cần convert, chỉ cần set trực tiếp
        listFilm.setData(movies);  // ✅ Đúng: ListFilm giờ nhận List<Movie>
        return listFilm;
    }
    // ===== CÁC PHƯƠNG THỨC KHÁC GIỮ NGUYÊN =====
    private void saveSearchHistory(String query) {
        new Thread(() -> {
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
                Log.e("ExploreActivity", "Error saving search history: " + e.getMessage());
            }
        }).start();
    }

    private void applyFilters() {
        searchProgressBar.setVisibility(View.VISIBLE);
        showSearchResults();
        resultsCountTxt.setText("🎛️ Đang áp dụng bộ lọc...");

        new Thread(() -> {
            try {
                List<Movie> allMovies = database.movieDao().getAllMovies();
                List<Movie> filteredMovies = new ArrayList<>();

                Set<String> selectedGenres = filterManager.getSelectedGenres();
                int yearFrom = filterManager.getYearFrom();
                int yearTo = filterManager.getYearTo();
                String sortBy = filterManager.getSortBy();

                Log.d("ExploreActivity", "Applying filters - Genres: " + selectedGenres.size() +
                        ", Year range: " + yearFrom + "-" + yearTo + ", Sort: " + sortBy);

                // ✅ LỌC THEO GENRES VÀ YEAR
                for (Movie movie : allMovies) {
                    boolean includeMovie = true;

                    // Filter by genres
                    if (!selectedGenres.isEmpty()) {
                        boolean hasMatchingGenre = false;
                        if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
                            String[] movieGenres = movie.getGenres().split(",");
                            for (String movieGenre : movieGenres) {
                                String cleanMovieGenre = movieGenre.trim();
                                for (String selectedGenre : selectedGenres) {
                                    if (cleanMovieGenre.equalsIgnoreCase(selectedGenre)) {
                                        hasMatchingGenre = true;
                                        break;
                                    }
                                }
                                if (hasMatchingGenre) break;
                            }
                        }
                        if (!hasMatchingGenre) {
                            includeMovie = false;
                        }
                    }

                    // Filter by year range
                    if (includeMovie && (yearFrom > 1970 || yearTo < Calendar.getInstance().get(Calendar.YEAR))) {
                        if (movie.getYear() != null && !movie.getYear().isEmpty()) {
                            try {
                                int movieYear = Integer.parseInt(movie.getYear());
                                if (movieYear < yearFrom || movieYear > yearTo) {
                                    includeMovie = false;
                                }
                            } catch (NumberFormatException e) {
                                includeMovie = false;
                            }
                        } else {
                            includeMovie = false;
                        }
                    }

                    if (includeMovie) {
                        filteredMovies.add(movie);
                    }
                }

                // Sort theo criteria
                if ("imdb_rating".equals(sortBy)) {
                    filteredMovies.sort((m1, m2) -> {
                        try {
                            double rating1 = Double.parseDouble(m1.getImdbRating() != null ? m1.getImdbRating() : "0");
                            double rating2 = Double.parseDouble(m2.getImdbRating() != null ? m2.getImdbRating() : "0");
                            return Double.compare(rating2, rating1); // Descending
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    });
                } else if ("year".equals(sortBy)) {
                    filteredMovies.sort((m1, m2) -> {
                        String year1 = m1.getYear() != null ? m1.getYear() : "0";
                        String year2 = m2.getYear() != null ? m2.getYear() : "0";
                        return year2.compareTo(year1); // Descending
                    });
                } else { // title
                    filteredMovies.sort((m1, m2) -> {
                        String title1 = m1.getTitle() != null ? m1.getTitle() : "";
                        String title2 = m2.getTitle() != null ? m2.getTitle() : "";
                        return title1.compareToIgnoreCase(title2);
                    });
                }

                runOnUiThread(() -> {
                    searchProgressBar.setVisibility(View.GONE);

                    if (!filteredMovies.isEmpty()) {
                        ListFilm listFilm = convertMoviesToListFilm(filteredMovies);

                        // Tạo filter summary
                        StringBuilder filterSummary = new StringBuilder("🎯 ");
                        filterSummary.append(filteredMovies.size()).append(" kết quả");

                        if (!selectedGenres.isEmpty()) {
                            filterSummary.append(" - Thể loại: ").append(selectedGenres.size());
                        }
                        if (yearFrom > 1970 || yearTo < Calendar.getInstance().get(Calendar.YEAR)) {
                            filterSummary.append(" - Năm: ").append(yearFrom).append("-").append(yearTo);
                        }

                        resultsCountTxt.setText(filterSummary.toString());
                        adapterSearchResults = new FilmListAdapter(listFilm);
                        exploreRecyclerView.setAdapter(adapterSearchResults);
                    } else {
                        resultsCountTxt.setText("❌ Không tìm thấy kết quả phù hợp với bộ lọc");
                        ListFilm emptyListFilm = new ListFilm();
                        emptyListFilm.setData(new ArrayList<>());
                        exploreRecyclerView.setAdapter(new FilmListAdapter(emptyListFilm));
                    }
                });

            } catch (Exception e) {
                Log.e("ExploreActivity", "Error applying filters: " + e.getMessage());
                runOnUiThread(() -> {
                    searchProgressBar.setVisibility(View.GONE);
                    resultsCountTxt.setText("⚠️ Đã xảy ra lỗi khi áp dụng bộ lọc");
                });
            }
        }).start();
    }

    private void setupBottomNavigation() {
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
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

    private void highlightCurrentTab() {
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        int selectedColor = ContextCompat.getColor(this, R.color.selected_tab_color);

        btnHistory.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnFavorites.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnSearch.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN);
        btnProfile.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
    }

    private void setupSearchListeners() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdatingText) {
                    return;
                }

                if (s.length() > 0) {
                    showCloseIcon();

                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }

                    searchRunnable = () -> performRealtimeSearch(s.toString().trim());
                    searchHandler.postDelayed(searchRunnable, SEARCH_DELAY);

                } else {
                    hideCloseIcon();
                    clearSearchResultsOnly();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Touch listener cho icons
        searchEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() != MotionEvent.ACTION_UP) {
                return false;
            }

            Drawable drawableLeft = searchEditText.getCompoundDrawables()[0];
            if (drawableLeft != null && event.getRawX() <= (searchEditText.getLeft()
                    + drawableLeft.getBounds().width() + searchEditText.getPaddingStart())) {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    performRealtimeSearch(query);
                }
                return true;
            }

            Drawable drawableRight = searchEditText.getCompoundDrawables()[2];
            if (drawableRight != null && event.getRawX() >= (searchEditText.getRight()
                    - drawableRight.getBounds().width() - searchEditText.getPaddingEnd())) {
                clearSearchSafely();
                return true;
            }

            return false;
        });

        // Enter key
        searchEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    performRealtimeSearch(query);
                }
                return true;
            }
            return false;
        });
    }

    private void clearSearchSafely() {
        try {
            if (searchHandler != null && searchRunnable != null) {
                searchHandler.removeCallbacks(searchRunnable);
            }

            isSearching = false;
            isUpdatingText = true;

            searchEditText.setText("");
            isUpdatingText = false;

            clearSearchResultsOnly();

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            }
            searchEditText.clearFocus();

        } catch (Exception e) {
            Log.e("ExploreActivity", "Error in clearSearchSafely: " + e.getMessage());
            isUpdatingText = false;
        }
    }

    private void clearSearchResultsOnly() {
        try {
            if (searchHandler != null && searchRunnable != null) {
                searchHandler.removeCallbacks(searchRunnable);
            }

            isSearching = false;

            if (searchResultsContainer != null) {
                searchResultsContainer.setVisibility(View.GONE);
            }

            if (searchProgressBar != null) {
                searchProgressBar.setVisibility(View.GONE);
            }

            if (trendingSection != null) {
                trendingSection.setVisibility(View.VISIBLE);
            }

            if (categorySection != null) {
                categorySection.setVisibility(View.VISIBLE);
            }

            if (resultsCountTxt != null) {
                resultsCountTxt.setText("");
                resultsCountTxt.setVisibility(View.GONE);
            }

            hideCloseIcon();

        } catch (Exception e) {
            Log.e("ExploreActivity", "Error in clearSearchResultsOnly: " + e.getMessage());
        }
    }

    private void showCloseIcon() {
        Drawable[] drawables = searchEditText.getCompoundDrawables();
        searchEditText.setCompoundDrawables(
                drawables[0], // search icon
                drawables[1],
                closeIcon,    // close icon
                drawables[3]
        );
    }

    private void hideCloseIcon() {
        Drawable[] drawables = searchEditText.getCompoundDrawables();
        searchEditText.setCompoundDrawables(
                drawables[0], // search icon
                drawables[1],
                null,         // no close icon
                drawables[3]
        );
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_filter_dialog, null);
        builder.setView(dialogView);

        if (filterManager == null) {
            filterManager = new FilterManager(this);
        }

        Spinner yearFromSpinner = dialogView.findViewById(R.id.yearFromSpinner);
        Spinner yearToSpinner = dialogView.findViewById(R.id.yearToSpinner);

        setupYearSpinners(yearFromSpinner, yearToSpinner);

        LinearLayout genreContainer = dialogView.findViewById(R.id.genreCheckboxContainer);
        List<CheckBox> genreCheckBoxes = new ArrayList<>();
        Set<String> selectedGenres = filterManager.getSelectedGenres();

        loadGenresFromDatabase(genreContainer, genreCheckBoxes, selectedGenres);

        RadioGroup sortGroup = dialogView.findViewById(R.id.sortByRadioGroup);
        String currentSort = filterManager.getSortBy();
        if ("title".equals(currentSort)) {
            ((RadioButton) dialogView.findViewById(R.id.sortByTitle)).setChecked(true);
        } else if ("imdb_rating".equals(currentSort)) {
            ((RadioButton) dialogView.findViewById(R.id.sortByRating)).setChecked(true);
        } else if ("year".equals(currentSort)) {
            ((RadioButton) dialogView.findViewById(R.id.sortByYear)).setChecked(true);
        }

        Button applyBtn = dialogView.findViewById(R.id.applyFilterBtn);
        final AlertDialog dialog = builder.create();

        applyBtn.setOnClickListener(v -> {
            Set<String> genresToSave = new HashSet<>();
            for (CheckBox cb : genreCheckBoxes) {
                if (cb.isChecked()) {
                    genresToSave.add((String) cb.getTag());
                }
            }
            filterManager.saveGenres(genresToSave);

            String yearFromStr = yearFromSpinner.getSelectedItem().toString();
            String yearToStr = yearToSpinner.getSelectedItem().toString();

            if (!"Tất cả".equals(yearFromStr) && !"Tất cả".equals(yearToStr)) {
                try {
                    int yearFrom = Integer.parseInt(yearFromStr);
                    int yearTo = Integer.parseInt(yearToStr);
                    filterManager.saveYearRange(yearFrom, yearTo);
                } catch (NumberFormatException e) {
                    Log.e("ExploreActivity", "Error parsing year range: " + e.getMessage());
                }
            }

            int checkedId = sortGroup.getCheckedRadioButtonId();
            String sortBy = "title";
            if (checkedId == R.id.sortByRating) {
                sortBy = "imdb_rating";
            } else if (checkedId == R.id.sortByYear) {
                sortBy = "year";
            }
            filterManager.saveSortBy(sortBy);

            applyFilters();
            dialog.dismiss();
        });

        Button resetBtn = dialogView.findViewById(R.id.resetFilterBtn);
        resetBtn.setOnClickListener(v -> {
            filterManager.resetFilters();
            clearSearch();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupYearSpinners(Spinner yearFromSpinner, Spinner yearToSpinner) {
        List<String> years = new ArrayList<>();
        years.add("Tất cả");

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int year = currentYear; year >= 1970; year--) {
            years.add(String.valueOf(year));
        }

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, years) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(getResources().getColor(android.R.color.black));
                return view;
            }
        };
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        yearFromSpinner.setAdapter(yearAdapter);
        yearToSpinner.setAdapter(yearAdapter);

        int savedYearFrom = filterManager.getYearFrom();
        int savedYearTo = filterManager.getYearTo();

        if (savedYearFrom > 1970) {
            String yearFromStr = String.valueOf(savedYearFrom);
            int fromPosition = years.indexOf(yearFromStr);
            if (fromPosition >= 0) {
                yearFromSpinner.setSelection(fromPosition);
            }
        }

        if (savedYearTo < currentYear) {
            String yearToStr = String.valueOf(savedYearTo);
            int toPosition = years.indexOf(yearToStr);
            if (toPosition >= 0) {
                yearToSpinner.setSelection(toPosition);
            }
        }

        Log.d("ExploreActivity", "Year spinners setup complete. Range: " + savedYearFrom + " - " + savedYearTo);
    }

    private void loadGenresFromDatabase(LinearLayout genreContainer,
                                        List<CheckBox> genreCheckBoxes,
                                        Set<String> selectedGenres) {

        TextView loadingText = new TextView(this);
        loadingText.setText("Đang tải thể loại...");
        loadingText.setTextColor(getResources().getColor(android.R.color.white));
        genreContainer.addView(loadingText);

        new Thread(() -> {
            try {
                List<String> genreStrings = database.movieDao().getAllGenres();
                List<String> uniqueGenres = GenreHelper.extractGenresFromDatabase(genreStrings);

                runOnUiThread(() -> {
                    genreContainer.removeView(loadingText);

                    if (uniqueGenres.isEmpty()) {
                        TextView noGenresText = new TextView(this);
                        noGenresText.setText("Không có thể loại nào trong CSDL");
                        noGenresText.setTextColor(getResources().getColor(android.R.color.white));
                        genreContainer.addView(noGenresText);
                    } else {
                        for (String genre : uniqueGenres) {
                            CheckBox checkBox = new CheckBox(this);

                            String displayName = GenreHelper.getGenreDisplayName(genre);
                            checkBox.setText(displayName);
                            checkBox.setTextColor(getResources().getColor(android.R.color.white));

                            checkBox.setTag(genre);
                            checkBox.setChecked(selectedGenres.contains(genre));

                            genreContainer.addView(checkBox);
                            genreCheckBoxes.add(checkBox);
                        }
                    }

                    Log.d("ExploreActivity", "Loaded " + uniqueGenres.size() + " genres from database");
                });

            } catch (Exception e) {
                Log.e("ExploreActivity", "Error loading genres from database: " + e.getMessage());
                runOnUiThread(() -> {
                    genreContainer.removeView(loadingText);

                    TextView errorText = new TextView(this);
                    errorText.setText("Lỗi khi tải thể loại");
                    errorText.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    genreContainer.addView(errorText);
                });
            }
        }).start();
    }

    private void showSearchResults() {
        trendingSection.setVisibility(View.GONE);
        categorySection.setVisibility(View.GONE);
        searchResultsContainer.setVisibility(View.VISIBLE);
    }

    private void clearSearch() {
        clearSearchSafely();
    }

    private void performRealtimeSearch(String query) {
        if (query.isEmpty()) {
            clearSearchResultsOnly();
            return;
        }

        if (isSearching) {
            return;
        }

        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }

        isSearching = true;

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        }

        showSearchResults();

        if (query.length() >= 2) {
            searchProgressBar.setVisibility(View.VISIBLE);
            resultsCountTxt.setText("🔍 Đang tìm kiếm \"" + query + "\"...");
        }

        new Thread(() -> {
            try {
                if (query.length() < 3) {
                    Thread.sleep(200);
                }

                List<Movie> searchResults = database.movieDao().searchMovies("%" + query + "%");

                runOnUiThread(() -> {
                    isSearching = false;

                    if (searchProgressBar != null) {
                        searchProgressBar.setVisibility(View.GONE);
                    }

                    if (searchResults != null && !searchResults.isEmpty()) {
                        ListFilm listFilm = convertMoviesToListFilm(searchResults);

                        if (resultsCountTxt != null) {
                            resultsCountTxt.setText("🔍 Tìm thấy " + searchResults.size() + " kết quả cho \"" + query + "\"");
                        }

                        if (exploreRecyclerView != null) {
                            adapterSearchResults = new FilmListAdapter(listFilm);
                            exploreRecyclerView.setAdapter(adapterSearchResults);
                        }
                    } else {
                        if (resultsCountTxt != null) {
                            resultsCountTxt.setText("❌ Không tìm thấy kết quả cho \"" + query + "\"");
                        }

                        if (exploreRecyclerView != null) {
                            exploreRecyclerView.setAdapter(new FilmListAdapter(new ListFilm()));
                        }
                    }
                });

                if (query.length() >= 3) {
                    saveSearchHistory(query);
                }

            } catch (InterruptedException e) {
                runOnUiThread(() -> isSearching = false);
            } catch (Exception e) {
                Log.e("ExploreActivity", "Error in realtime search: " + e.getMessage());
                runOnUiThread(() -> {
                    isSearching = false;
                    if (searchProgressBar != null) {
                        searchProgressBar.setVisibility(View.GONE);
                    }
                    if (resultsCountTxt != null) {
                        resultsCountTxt.setText("⚠️ Đã xảy ra lỗi khi tìm kiếm");
                    }
                });
            }
        }).start();
    }

    private void focusSearchBox() {
        searchEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void setFabToExplorePosition() {
        BaseBottomNavigationHelper.setFabPositionImmediate(
                bottomAppBar,
                fabHome,
                BaseBottomNavigationHelper.SEARCH_POSITION
        );
    }

    protected void onDestroy() {
        super.onDestroy();

        try {
            if (searchHandler != null && searchRunnable != null) {
                searchHandler.removeCallbacks(searchRunnable);
            }

            isUpdatingText = false;
            isSearching = false;

        } catch (Exception e) {
            Log.e("ExploreActivity", "Error in onDestroy cleanup: " + e.getMessage());
        }
    }
}