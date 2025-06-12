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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.movies_app.Adapter.FilmListAdapter;
import com.example.movies_app.Domain.ListFilm;
import com.example.movies_app.Helper.BaseBottomNavigationHelper;
import com.example.movies_app.Helper.FilterManager;
import com.example.movies_app.R;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExploreActivity extends AppCompatActivity {

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

        filterManager = new FilterManager(this);
        initViews();
        setupBottomNavigation();
        highlightCurrentTab();
        setupSearchListeners();
        focusSearchBox();
        loadTrendingMovies();
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

    }

    private void setupBottomNavigation() {
        btnHistory.setOnClickListener(v -> {


            fabHome.postDelayed(() -> {
                Intent intent = new Intent(this, HistoryActivity.class);
                startActivity(intent);
            }, 1000);
        });
        btnFavorites.setOnClickListener(v -> {


            fabHome.postDelayed(() -> {
                Intent intent = new Intent(this, FavoriteActivity.class);
                startActivity(intent);
            }, 1000);
        });
        btnMain.setOnClickListener(v -> {
            bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);

            fabHome.postDelayed(() -> {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }, 1000);
        });



        btnSearch.setOnClickListener(v -> {
            fabHome.postDelayed(() -> {
                Intent intent = new Intent(this, ExploreActivity.class);
                startActivity(intent);
            }, 1000);
        });

        // ✅ SỬA BUTTON PROFILE - Thêm animation FAB
        btnProfile.setOnClickListener(v -> {

            fabHome.postDelayed(() -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
            }, 1000);
        });
    }


    private void highlightCurrentTab() {
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        int selectedColor = ContextCompat.getColor(this, R.color.selected_tab_color);

        btnHistory.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnFavorites.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnSearch.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN); // ✅ Highlight Search
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

        // Tạo URL tìm kiếm
        String searchUrl = "https://moviesapi.ir/api/v1/movies?q=" + query;

        RequestQueue searchQueue = Volley.newRequestQueue(this);
        StringRequest searchRequest = new StringRequest(Request.Method.GET, searchUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        try {
                            ListFilm items = gson.fromJson(response, ListFilm.class);

                            if (items != null && items.getData() != null) {
                                int resultCount = items.getData().size();
                                resultsCountTxt.setText("🔍 Tìm thấy " + resultCount + " kết quả cho \"" + query + "\"");

                                adapterSearchResults = new FilmListAdapter(items);
                                exploreRecyclerView.setAdapter(adapterSearchResults);
                            } else {
                                resultsCountTxt.setText("❌ Không tìm thấy kết quả cho \"" + query + "\"");
                            }

                            searchProgressBar.setVisibility(View.GONE);
                        } catch (Exception e) {
                            Log.e("ExploreActivity", "Error parsing JSON: " + e.getMessage());
                            searchProgressBar.setVisibility(View.GONE);
                            resultsCountTxt.setText("⚠️ Đã xảy ra lỗi khi tìm kiếm");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ExploreActivity", "Error: " + error.toString());
                        searchProgressBar.setVisibility(View.GONE);
                        resultsCountTxt.setText("🌐 Không thể kết nối đến máy chủ");
                    }
                });

        searchQueue.add(searchRequest);
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_filter_dialog, null);
        builder.setView(dialogView);

        // Khởi tạo FilterManager nếu chưa có
        if (filterManager == null) {
            filterManager = new FilterManager(this);
        }

        // Tạo danh sách thể loại
        final String[][] genresList = {
                {"28", "Hành Động"},
                {"12", "Phiêu Lưu"},
                {"16", "Hoạt Hình"},
                {"35", "Hài"},
                {"80", "Tội Phạm"},
                {"18", "Chính Kịch"},
                {"14", "Giả Tưởng"},
                {"27", "Kinh Dị"},
                {"10749", "Lãng Mạn"},
                {"878", "Khoa Học Viễn Tưởng"}
        };

        // Hiển thị các checkbox thể loại
        LinearLayout genreContainer = dialogView.findViewById(R.id.genreCheckboxContainer);
        List<CheckBox> genreCheckBoxes = new ArrayList<>();
        Set<String> selectedGenres = filterManager.getSelectedGenres();

        // Tạo các checkbox thể loại
        for (String[] genre : genresList) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(genre[1]);
            checkBox.setTextColor(getResources().getColor(android.R.color.white));
            checkBox.setTag(genre[0]);
            checkBox.setChecked(selectedGenres.contains(genre[0]));
            genreContainer.addView(checkBox);
            genreCheckBoxes.add(checkBox);
        }

        // Thiết lập radio buttons sắp xếp
        RadioGroup sortGroup = dialogView.findViewById(R.id.sortByRadioGroup);
        String currentSort = filterManager.getSortBy();
        if ("title".equals(currentSort)) {
            ((RadioButton) dialogView.findViewById(R.id.sortByTitle)).setChecked(true);
        } else if ("imdb_rating".equals(currentSort)) {
            ((RadioButton) dialogView.findViewById(R.id.sortByRating)).setChecked(true);
        } else if ("year".equals(currentSort)) {
            ((RadioButton) dialogView.findViewById(R.id.sortByYear)).setChecked(true);
        }

        // Thiết lập nút áp dụng
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

    private void applyFilters() {
        // Hiển thị loading
        searchProgressBar.setVisibility(View.VISIBLE);
        showSearchResults();

        // Tạo URL với bộ lọc
        String baseUrl = "https://moviesapi.ir/api/v1/movies";
        String url = filterManager.buildFilterUrl(baseUrl);

        // Hiện tiêu đề
        resultsCountTxt.setText("🎛️ Đang áp dụng bộ lọc...");

        // Gửi request
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    searchProgressBar.setVisibility(View.GONE);

                    try {
                        Gson gson = new Gson();
                        ListFilm items = gson.fromJson(response, ListFilm.class);

                        if (items != null && items.getData() != null) {
                            // Hiển thị kết quả
                            int resultCount = items.getData().size();
                            resultsCountTxt.setText("🎯 Tìm thấy " + resultCount + " kết quả phù hợp");

                            // Thiết lập adapter
                            adapterSearchResults = new FilmListAdapter(items);
                            exploreRecyclerView.setAdapter(adapterSearchResults);
                        } else {
                            resultsCountTxt.setText("❌ Không tìm thấy kết quả phù hợp");
                        }
                    } catch (Exception e) {
                        Log.e("ExploreActivity", "Error parsing JSON: " + e.getMessage());
                        resultsCountTxt.setText("⚠️ Đã xảy ra lỗi khi áp dụng bộ lọc");
                    }
                },
                error -> {
                    searchProgressBar.setVisibility(View.GONE);
                    resultsCountTxt.setText("🌐 Không thể kết nối đến máy chủ");
                });

        queue.add(request);
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

        // Icon X sẽ tự động ẩn vì TextWatcher
    }

    private void loadTrendingMovies() {
        // Load phim trending từ API
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET,
                "https://moviesapi.ir/api/v1/movies?page=2",
                response -> {
                    Gson gson = new Gson();
                    ListFilm items = gson.fromJson(response, ListFilm.class);
                    FilmListAdapter adapter = new FilmListAdapter(items);
                    trendingRecyclerView.setAdapter(adapter);
                },
                error -> Log.e("ExploreActivity", "Error loading trending: " + error.toString()));

        queue.add(request);
    }

    private void focusSearchBox() {
        searchEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
    }
    private void setFabToExplorePosition() {
        // ✅ Sử dụng helper
        BaseBottomNavigationHelper.setFabPosition(
                bottomAppBar,
                fabHome,
                BaseBottomNavigationHelper.SEARCH_POSITION
        );
    }
}