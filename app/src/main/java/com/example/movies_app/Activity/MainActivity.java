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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.movies_app.Adapter.FilmListAdapter;
import com.example.movies_app.Domain.ListFilm;
import com.example.movies_app.R;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
public class MainActivity extends AppCompatActivity {
    private RecyclerView.Adapter adapterNewMovies, adapterUpComing, adapterSearchResults;
    private RecyclerView recyclerViewNewMovies, recyclerViewUpComing, homeSearchRecyclerView;
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest, mStringRequest2;
    private ProgressBar loading1, loading2, homeSearchProgressBar;

    // Search components
    private EditText homeSearchEditText;
    private LinearLayout homeSearchResultsContainer, homeMainContent;
    private TextView homeResultsCountTxt;
    private Drawable closeIcon;

    // Bottom Navigation Components
    private BottomAppBar bottomAppBar;
    private ImageView btnMain, btnHistory, btnFavorites, btnSearch, btnProfile;
    private FloatingActionButton fabHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupBottomNavigation();
        setupSearchListeners();
        sendRequest1();
        sendRequest2();
        highlightHomeTab();

        // Setup close icon
        closeIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_close, null);
        if (closeIcon != null) {
            closeIcon.setBounds(0, 0, closeIcon.getIntrinsicWidth(), closeIcon.getIntrinsicHeight());
        }
        hideCloseIcon();
    }

    private void initViews() {
        // ✅ THÊM BottomAppBar reference
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

        // Setup search RecyclerView
        homeSearchRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
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
            // Giữ nguyên cho FAB click
            bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);

            fabHome.postDelayed(() -> {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }, 100);
        });
    }
    // ✅ THÊM METHOD HELPER chuyển đổi dp sang px
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void setupSearchListeners() {
        // Enter key để search
        homeSearchEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch();
                return true;
            }
            return false;
        });

        // Hiển thị/ẩn icon X khi có text
        homeSearchEditText.addTextChangedListener(new TextWatcher() {
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
        homeSearchEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() != MotionEvent.ACTION_UP) {
                return false;
            }

            // Click vào icon search (bên trái)
            Drawable drawableLeft = homeSearchEditText.getCompoundDrawables()[0];
            if (drawableLeft != null && event.getRawX() <= (homeSearchEditText.getLeft()
                    + drawableLeft.getBounds().width() + homeSearchEditText.getPaddingStart())) {
                performSearch();
                return true;
            }

            // Click vào icon close (bên phải)
            Drawable drawableRight = homeSearchEditText.getCompoundDrawables()[2];
            if (drawableRight != null && event.getRawX() >= (homeSearchEditText.getRight()
                    - drawableRight.getBounds().width() - homeSearchEditText.getPaddingEnd())) {
                clearSearch();
                return true;
            }

            return false;
        });
    }

    private void showCloseIcon() {
        Drawable[] drawables = homeSearchEditText.getCompoundDrawables();
        homeSearchEditText.setCompoundDrawables(
                drawables[0], // search icon
                drawables[1],
                closeIcon,    // close icon
                drawables[3]
        );
    }

    private void hideCloseIcon() {
        Drawable[] drawables = homeSearchEditText.getCompoundDrawables();
        homeSearchEditText.setCompoundDrawables(
                drawables[0], // search icon
                drawables[1],
                null,         // no close icon
                drawables[3]
        );
    }

    private void performSearch() {
        String query = homeSearchEditText.getText().toString().trim();

        if (query.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ẩn bàn phím
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(homeSearchEditText.getWindowToken(), 0);

        // Hiển thị kết quả tìm kiếm
        showSearchResults();
        homeSearchProgressBar.setVisibility(View.VISIBLE);

        // Tạo URL tìm kiếm
        String searchUrl = "https://moviesapi.ir/api/v1/movies?q=" + query;

        RequestQueue searchQueue = Volley.newRequestQueue(this);
        StringRequest searchRequest = new StringRequest(Request.Method.GET, searchUrl,
                response -> {
                    Gson gson = new Gson();
                    try {
                        ListFilm items = gson.fromJson(response, ListFilm.class);

                        if (items != null && items.getData() != null) {
                            int resultCount = items.getData().size();
                            homeResultsCountTxt.setText("🔍 Tìm thấy " + resultCount + " kết quả cho \"" + query + "\"");

                            adapterSearchResults = new FilmListAdapter(items);
                            homeSearchRecyclerView.setAdapter(adapterSearchResults);
                        } else {
                            homeResultsCountTxt.setText("❌ Không tìm thấy kết quả cho \"" + query + "\"");
                        }

                        homeSearchProgressBar.setVisibility(View.GONE);
                    } catch (Exception e) {
                        Log.e("MainActivity", "Error parsing JSON: " + e.getMessage());
                        homeSearchProgressBar.setVisibility(View.GONE);
                        homeResultsCountTxt.setText("⚠️ Đã xảy ra lỗi khi tìm kiếm");
                    }
                },
                error -> {
                    Log.e("MainActivity", "Error: " + error.toString());
                    homeSearchProgressBar.setVisibility(View.GONE);
                    homeResultsCountTxt.setText("🌐 Không thể kết nối đến máy chủ");
                });

        searchQueue.add(searchRequest);
    }

    private void showSearchResults() {
        // Ẩn nội dung chính
        homeMainContent.setVisibility(View.GONE);

        // Hiển thị kết quả tìm kiếm
        homeSearchResultsContainer.setVisibility(View.VISIBLE);
    }

    private void clearSearch() {
        homeSearchEditText.setText("");

        // Ẩn kết quả tìm kiếm
        homeSearchResultsContainer.setVisibility(View.GONE);

        // Hiển thị lại nội dung chính
        homeMainContent.setVisibility(View.VISIBLE);

        // Ẩn bàn phím và xóa focus
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(homeSearchEditText.getWindowToken(), 0);
        homeSearchEditText.clearFocus();

        // Icon X sẽ tự động ẩn vì TextWatcher
    }

    private void highlightHomeTab() {
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);

        btnHistory.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnFavorites.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnSearch.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnProfile.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);

        // FAB Home được highlight bằng cách là trang hiện tại
        fabHome.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.selected_tab_color));
    }

    private void sendRequest1() {
        mRequestQueue = Volley.newRequestQueue(this);
        loading1.setVisibility(android.view.View.VISIBLE);
        mStringRequest = new StringRequest(Request.Method.GET, "https://moviesapi.ir/api/v1/movies?page=1", response -> {
            Gson gson = new Gson();
            loading1.setVisibility(android.view.View.GONE);
            ListFilm items = gson.fromJson(response, ListFilm.class);
            adapterNewMovies = new FilmListAdapter(items);
            recyclerViewNewMovies.setAdapter(adapterNewMovies);
        }, error -> {
            loading1.setVisibility(android.view.View.GONE);
        });
        mRequestQueue.add(mStringRequest);
    }

    private void sendRequest2() {
        mRequestQueue = Volley.newRequestQueue(this);
        loading2.setVisibility(android.view.View.VISIBLE);
        mStringRequest2 = new StringRequest(Request.Method.GET, "https://moviesapi.ir/api/v1/movies?page=3", response -> {
            Gson gson = new Gson();
            loading2.setVisibility(android.view.View.GONE);
            ListFilm items = gson.fromJson(response, ListFilm.class);
            adapterUpComing = new FilmListAdapter(items);
            recyclerViewUpComing.setAdapter(adapterUpComing);
        }, error -> {
            loading2.setVisibility(android.view.View.GONE);
        });
        mRequestQueue.add(mStringRequest2);
    }
}