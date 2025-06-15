package com.example.movies_app.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import com.example.movies_app.Domain.Datum;
import com.example.movies_app.Domain.ListFilm;
import com.example.movies_app.Helper.BaseBottomNavigationHelper;
import com.example.movies_app.Helper.FilterManager;
import com.example.movies_app.Helper.GenreHelper;
import com.example.movies_app.R;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExploreActivity extends AppCompatActivity {

    // ✅ THÊM DATABASE VÀ ADAPTERS
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

    // Content sections
    private LinearLayout trendingSection, categorySection;

    // Bottom Navigation Components
    private ImageView btnMain, btnHistory, btnFavorites, btnSearch, btnProfile;
    private FloatingActionButton fabHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        // ✅ KHỞI TẠO DATABASE
        database = AppDatabase.getInstance(this);

        filterManager = new FilterManager(this);
        initViews();
        setupBottomNavigation();
        highlightCurrentTab();
        setupSearchListeners();
        focusSearchBox();

        loadTrendingMoviesFromDB();
        loadPopularCategoriesFromDB();

        setFabToExplorePosition();

        // Setup close icon
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

        // ✅ XÓA CÁC EmptyAdapter - sẽ được set trong load methods
    }

    // ✅ LOAD TRENDING MOVIES TỪ DATABASE
    private void loadTrendingMoviesFromDB() {
        new Thread(() -> {
            try {
                // Lấy tất cả movies từ database
                List<Movie> movies = database.movieDao().getAllMovies();

                if (movies != null && !movies.isEmpty()) {
                    // Convert sang ListFilm format
                    ListFilm listFilm = convertMoviesToListFilm(movies);

                    // Update UI trên main thread
                    runOnUiThread(() -> {
                        displayTrendingMovies(listFilm);
                        Log.d("ExploreActivity", "Loaded " + movies.size() + " trending movies from DB");
                    });
                } else {
                    runOnUiThread(() -> {
                        Log.d("ExploreActivity", "No movies found in database");
                        Toast.makeText(this, "Chưa có phim trong cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e("ExploreActivity", "Error loading trending movies from DB: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi tải phim thịnh hành", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void loadPopularCategoriesFromDB() {
        new Thread(() -> {
            try {
                List<Movie> highRatedMovies = database.movieDao().getAllMovies();

                if (highRatedMovies != null && !highRatedMovies.isEmpty()) {
                    List<Movie> filteredMovies = filterHighRatedMovies(highRatedMovies);
                    ListFilm listFilm = convertMoviesToListFilm(filteredMovies);

                    runOnUiThread(() -> {
                        displayCategories(listFilm);
                        Log.d("ExploreActivity", "Loaded " + filteredMovies.size() + " popular movies from DB");
                    });
                } else {
                    runOnUiThread(() -> {
                        Log.d("ExploreActivity", "No popular movies found");
                    });
                }
            } catch (Exception e) {
                Log.e("ExploreActivity", "Error loading popular categories from DB: " + e.getMessage());
            }
        }).start();
    }
    private List<Movie> filterHighRatedMovies(List<Movie> allMovies) {
        List<Movie> popularMovies = new ArrayList<>();

        for (Movie movie : allMovies) {
            try {
                if (movie.getImdbRating() != null &&
                        !movie.getImdbRating().isEmpty() &&
                        Double.parseDouble(movie.getImdbRating()) >= 7.0) {
                    popularMovies.add(movie);
                }
            } catch (NumberFormatException e) {
                // Skip movies with invalid rating
            }
        }

        // Giới hạn số lượng hiển thị
        if (popularMovies.size() > 10) {
            return popularMovies.subList(0, 10);
        }

        return popularMovies;
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

    // ✅ CONVERT MOVIES TO LISTFILM FORMAT
    private ListFilm convertMoviesToListFilm(List<Movie> movies) {
        ListFilm listFilm = new ListFilm();
        List<Datum> dataList = new ArrayList<>();

        for (Movie movie : movies) {
            Datum datum = new Datum();
            datum.setId(movie.getId());
            datum.setTitle(movie.getTitle());
            datum.setPoster(movie.getPoster());
            datum.setYear(movie.getYear());
            datum.setCountry(movie.getCountry());
            datum.setImdbRating(movie.getImdbRating());

            // Convert genres string back to list
            if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
                List<String> genres = Arrays.asList(movie.getGenres().split(","));
                datum.setGenres(genres);
            }

            // Convert images string back to list
            if (movie.getImages() != null && !movie.getImages().isEmpty()) {
                List<String> images = Arrays.asList(movie.getImages().split(","));
                datum.setImages(images);
            }

            dataList.add(datum);
        }

        listFilm.setData(dataList);
        return listFilm;
    }

    // ✅ CẬP NHẬT SEARCH TỪ DATABASE
    private void performSearch() {
        String query = searchEditText.getText().toString().trim();

        if (query.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ẩn bàn phím
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

        // Hiển thị kết quả tìm kiếm
        showSearchResults();
        searchProgressBar.setVisibility(View.VISIBLE);

        // ✅ TÌM KIẾM TỪ DATABASE
        new Thread(() -> {
            try {
                // Search trong database
                List<Movie> searchResults = database.movieDao().searchMovies("%" + query + "%");

                runOnUiThread(() -> {
                    searchProgressBar.setVisibility(View.GONE);

                    if (searchResults != null && !searchResults.isEmpty()) {
                        ListFilm listFilm = convertMoviesToListFilm(searchResults);

                        resultsCountTxt.setText("🔍 Tìm thấy " + searchResults.size() + " kết quả cho \"" + query + "\"");
                        adapterSearchResults = new FilmListAdapter(listFilm);
                        exploreRecyclerView.setAdapter(adapterSearchResults);
                    } else {
                        resultsCountTxt.setText("❌ Không tìm thấy kết quả cho \"" + query + "\"");
                        // Set empty adapter
                        exploreRecyclerView.setAdapter(new FilmListAdapter(new ListFilm()));
                    }
                });
            } catch (Exception e) {
                Log.e("ExploreActivity", "Error searching movies: " + e.getMessage());
                runOnUiThread(() -> {
                    searchProgressBar.setVisibility(View.GONE);
                    resultsCountTxt.setText("⚠️ Đã xảy ra lỗi khi tìm kiếm");
                });
            }
        }).start();
    }

    // ✅ CẬP NHẬT APPLY FILTERS TỪ DATABASE
    private void applyFilters() {
        // Hiển thị loading
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
                                // Skip movies with invalid year format
                                includeMovie = false;
                            }
                        } else {
                            // Skip movies without year info when year filter is applied
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
                        emptyListFilm.setData(new ArrayList<>()); // đảm bảo không null!
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

    private void highlightCurrentTab() {
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        int selectedColor = ContextCompat.getColor(this, R.color.selected_tab_color);

        btnHistory.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnFavorites.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnSearch.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN);
        btnProfile.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
    }

    private void setupSearchListeners() {
        // Enter key để search
        searchEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch();
                return true;
            }
            return false;
        });

        // Hiển thị/ẩn icon X khi có text
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    showCloseIcon();
                } else {
                    hideCloseIcon();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Xử lý click vào icon search và close
        searchEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() != MotionEvent.ACTION_UP) {
                return false;
            }

            // Click vào icon search (bên trái)
            Drawable drawableLeft = searchEditText.getCompoundDrawables()[0];
            if (drawableLeft != null && event.getRawX() <= (searchEditText.getLeft()
                    + drawableLeft.getBounds().width() + searchEditText.getPaddingStart())) {
                performSearch();
                return true;
            }

            // Click vào icon close (bên phải)
            Drawable drawableRight = searchEditText.getCompoundDrawables()[2];
            if (drawableRight != null && event.getRawX() >= (searchEditText.getRight()
                    - drawableRight.getBounds().width() - searchEditText.getPaddingEnd())) {
                clearSearch();
                return true;
            }

            return false;
        });
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

        // Khởi tạo FilterManager nếu chưa có
        if (filterManager == null) {
            filterManager = new FilterManager(this);
        }

        // ========== THIẾT LẬP YEAR SPINNERS ==========
        Spinner yearFromSpinner = dialogView.findViewById(R.id.yearFromSpinner);
        Spinner yearToSpinner = dialogView.findViewById(R.id.yearToSpinner);

        setupYearSpinners(yearFromSpinner, yearToSpinner);

        // ========== THIẾT LẬP GENRES ==========
        LinearLayout genreContainer = dialogView.findViewById(R.id.genreCheckboxContainer);
        List<CheckBox> genreCheckBoxes = new ArrayList<>();
        Set<String> selectedGenres = filterManager.getSelectedGenres();

        // Load genres từ database
        loadGenresFromDatabase(genreContainer, genreCheckBoxes, selectedGenres);

        // ========== THIẾT LẬP SORT RADIO BUTTONS ==========
        RadioGroup sortGroup = dialogView.findViewById(R.id.sortByRadioGroup);
        String currentSort = filterManager.getSortBy();
        if ("title".equals(currentSort)) {
            ((RadioButton) dialogView.findViewById(R.id.sortByTitle)).setChecked(true);
        } else if ("imdb_rating".equals(currentSort)) {
            ((RadioButton) dialogView.findViewById(R.id.sortByRating)).setChecked(true);
        } else if ("year".equals(currentSort)) {
            ((RadioButton) dialogView.findViewById(R.id.sortByYear)).setChecked(true);
        }

        // ========== THIẾT LẬP BUTTONS ==========
        Button applyBtn = dialogView.findViewById(R.id.applyFilterBtn);
        final AlertDialog dialog = builder.create();

        applyBtn.setOnClickListener(v -> {
            // Lưu thể loại đã chọn
            Set<String> genresToSave = new HashSet<>();
            for (CheckBox cb : genreCheckBoxes) {
                if (cb.isChecked()) {
                    genresToSave.add((String) cb.getTag());
                }
            }
            filterManager.saveGenres(genresToSave);

            // Lưu year range
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

            // Lưu tùy chọn sắp xếp
            int checkedId = sortGroup.getCheckedRadioButtonId();
            String sortBy = "title";
            if (checkedId == R.id.sortByRating) {
                sortBy = "imdb_rating";
            } else if (checkedId == R.id.sortByYear) {
                sortBy = "year";
            }
            filterManager.saveSortBy(sortBy);

            // Áp dụng bộ lọc
            applyFilters();

            dialog.dismiss();
        });

        // Thiết lập nút đặt lại
        Button resetBtn = dialogView.findViewById(R.id.resetFilterBtn);
        resetBtn.setOnClickListener(v -> {
            filterManager.resetFilters();
            clearSearch();
            dialog.dismiss();
        });

        dialog.show();
    }
    private void setupYearSpinners(Spinner yearFromSpinner, Spinner yearToSpinner) {
        // Tạo danh sách năm từ 1970 đến năm hiện tại
        List<String> years = new ArrayList<>();
        years.add("Tất cả"); // Option đầu tiên

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int year = currentYear; year >= 1970; year--) { // Từ mới nhất đến cũ nhất
            years.add(String.valueOf(year));
        }

        // Tạo adapter cho spinner
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

        // Set adapter cho cả 2 spinner
        yearFromSpinner.setAdapter(yearAdapter);
        yearToSpinner.setAdapter(yearAdapter);

        // Set giá trị hiện tại từ FilterManager
        int savedYearFrom = filterManager.getYearFrom();
        int savedYearTo = filterManager.getYearTo();

        // Tìm và set position cho yearFrom
        if (savedYearFrom > 1970) {
            String yearFromStr = String.valueOf(savedYearFrom);
            int fromPosition = years.indexOf(yearFromStr);
            if (fromPosition >= 0) {
                yearFromSpinner.setSelection(fromPosition);
            }
        }

        // Tìm và set position cho yearTo
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

        // Hiển thị loading hoặc placeholder
        TextView loadingText = new TextView(this);
        loadingText.setText("Đang tải thể loại...");
        loadingText.setTextColor(getResources().getColor(android.R.color.white));
        genreContainer.addView(loadingText);

        // Load từ database trong background thread
        new Thread(() -> {
            try {
                // Lấy tất cả genre strings từ database
                List<String> genreStrings = database.movieDao().getAllGenres();

                // Xử lý để lấy unique genres
                List<String> uniqueGenres = GenreHelper.extractGenresFromDatabase(genreStrings);

                // Update UI trên main thread
                runOnUiThread(() -> {
                    // Xóa loading text
                    genreContainer.removeView(loadingText);

                    // Tạo checkboxes cho genres
                    if (uniqueGenres.isEmpty()) {
                        TextView noGenresText = new TextView(this);
                        noGenresText.setText("Không có thể loại nào trong CSDL");
                        noGenresText.setTextColor(getResources().getColor(android.R.color.white));
                        genreContainer.addView(noGenresText);
                    } else {
                        for (String genre : uniqueGenres) {
                            CheckBox checkBox = new CheckBox(this);

                            // Sử dụng display name cho UI
                            String displayName = GenreHelper.getGenreDisplayName(genre);
                            checkBox.setText(displayName);
                            checkBox.setTextColor(getResources().getColor(android.R.color.white));

                            // Sử dụng genre gốc làm tag để filter
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
                    // Xóa loading text
                    genreContainer.removeView(loadingText);

                    // Hiển thị error message
                    TextView errorText = new TextView(this);
                    errorText.setText("Lỗi khi tải thể loại");
                    errorText.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    genreContainer.addView(errorText);
                });
            }
        }).start();
    }
    private void showSearchResults() {
        // Ẩn trending và categories
        trendingSection.setVisibility(View.GONE);
        categorySection.setVisibility(View.GONE);

        // Hiển thị kết quả tìm kiếm
        searchResultsContainer.setVisibility(View.VISIBLE);
    }

    private void clearSearch() {
        searchEditText.setText("");

        // Ẩn kết quả tìm kiếm
        searchResultsContainer.setVisibility(View.GONE);

        // Hiển thị lại trending và categories
        trendingSection.setVisibility(View.VISIBLE);
        categorySection.setVisibility(View.VISIBLE);

        // Ẩn bàn phím và xóa focus
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        searchEditText.clearFocus();
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
}