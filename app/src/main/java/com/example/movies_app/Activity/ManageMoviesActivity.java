package com.example.movies_app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movies_app.Adapter.TMDbMovieAdapter;
import com.example.movies_app.Adapter.LocalMovieAdapter; // Adapter mới cho phim local
import com.example.movies_app.Database.AppDatabase;
import com.example.movies_app.Database.entity.Movie;
import com.example.movies_app.Domain.TMDbMovie;
import com.example.movies_app.Domain.TMDbSearchResponse;
import com.example.movies_app.Domain.TMDbVideoResponse;
import com.example.movies_app.Domain.TMDbVideo;
import com.example.movies_app.Helper.TMDbApiService;
import com.example.movies_app.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManageMoviesActivity extends AppCompatActivity implements TMDbMovieAdapter.OnMovieClickListener, LocalMovieAdapter.OnLocalMovieClickListener {

    private EditText searchEditText;
    private RecyclerView moviesRecyclerView;
    private ProgressBar progressBar;
    private TextView resultCountText, noResultsText;
    private Button btnToggleView, btnAddFromTMDb;

    private TMDbApiService apiService;
    private TMDbMovieAdapter tmdbAdapter;
    private LocalMovieAdapter localAdapter;
    private List<TMDbMovie> tmdbMoviesList;
    private List<Movie> localMoviesList;
    private AppDatabase databaseHelper;
    private ExecutorService executorService;

    private static final int REQUEST_ADD_MOVIE = 1001;
    private static final int REQUEST_EDIT_MOVIE = 1002;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean showingLocalMovies = true; // Mặc định hiển thị phim local

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_movies);

        initViews();
        setupRecyclerView();
        setupSearchListener();
        setupClickListeners();

        apiService = new TMDbApiService(this);
        databaseHelper = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        tmdbMoviesList = new ArrayList<>();
        localMoviesList = new ArrayList<>();

        // Load local movies by default
        loadLocalMovies();
    }

    private void initViews() {
        searchEditText = findViewById(R.id.movieSearchEditText);
        moviesRecyclerView = findViewById(R.id.moviesRecyclerView);
        progressBar = findViewById(R.id.searchProgressBar);
        resultCountText = findViewById(R.id.resultCountText);
        noResultsText = findViewById(R.id.noResultsText);
        btnToggleView = findViewById(R.id.btnToggleView);
        btnAddFromTMDb = findViewById(R.id.btnAddFromTMDb);
    }

    private void setupClickListeners() {
        btnToggleView.setOnClickListener(v -> toggleView());
        btnAddFromTMDb.setOnClickListener(v -> switchToTMDbSearch());
    }

    private void toggleView() {
        if (showingLocalMovies) {
            // Chuyển sang TMDb search
            switchToTMDbSearch();
        } else {
            // Chuyển về local movies
            switchToLocalMovies();
        }
    }

    private void switchToLocalMovies() {
        showingLocalMovies = true;
        btnToggleView.setText("Tìm phim từ TMDb");
        btnAddFromTMDb.setVisibility(View.GONE);
        searchEditText.setHint("Tìm kiếm trong phim đã thêm...");

        moviesRecyclerView.setAdapter(localAdapter);
        loadLocalMovies();
    }

    private void switchToTMDbSearch() {
        showingLocalMovies = false;
        btnToggleView.setText("Xem phim đã thêm");
        btnAddFromTMDb.setVisibility(View.VISIBLE);
        searchEditText.setHint("Tìm kiếm phim từ TMDb...");

        moviesRecyclerView.setAdapter(tmdbAdapter);
        loadPopularMovies();
    }

    private void loadLocalMovies() {
        if (isLoading) return;

        isLoading = true;
        showLoading(true);
        hideNoResults();

        executorService.execute(() -> {
            try {
                List<Movie> movies = databaseHelper.movieDao().getAllMovies();

                runOnUiThread(() -> {
                    isLoading = false;
                    showLoading(false);

                    localMoviesList.clear();
                    localMoviesList.addAll(movies);
                    localAdapter.updateMovies(localMoviesList);

                    if (movies.isEmpty()) {
                        showNoResults();
                        resultCountText.setText("Chưa có phim nào được thêm");
                    } else {
                        resultCountText.setText("Có " + movies.size() + " phim trong hệ thống");
                        resultCountText.setVisibility(View.VISIBLE);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    isLoading = false;
                    showLoading(false);
                    showNoResults();
                    Toast.makeText(ManageMoviesActivity.this, "Lỗi khi tải phim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void searchLocalMovies(String query) {
        if (isLoading) return;

        isLoading = true;
        showLoading(true);
        hideNoResults();

        executorService.execute(() -> {
            try {
                List<Movie> movies = databaseHelper.movieDao().searchMovies("%" + query + "%");

                runOnUiThread(() -> {
                    isLoading = false;
                    showLoading(false);

                    localMoviesList.clear();
                    localMoviesList.addAll(movies);
                    localAdapter.updateMovies(localMoviesList);

                    if (movies.isEmpty()) {
                        showNoResults();
                        resultCountText.setText("Không tìm thấy phim nào với từ khóa \"" + query + "\"");
                    } else {
                        resultCountText.setText("Tìm thấy " + movies.size() + " phim cho \"" + query + "\"");
                        resultCountText.setVisibility(View.VISIBLE);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    isLoading = false;
                    showLoading(false);
                    showNoResults();
                    Toast.makeText(ManageMoviesActivity.this, "Lỗi khi tìm kiếm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // Load popular movies when searching TMDb
    private void loadPopularMovies() {
        if (isLoading) return;

        isLoading = true;
        showLoading(true);
        hideNoResults();

        apiService.getPopularMovies(currentPage, new TMDbApiService.SearchCallback() {
            @Override
            public void onSuccess(TMDbSearchResponse response) {
                isLoading = false;
                showLoading(false);

                if (response.getResults() != null && !response.getResults().isEmpty()) {
                    tmdbMoviesList.clear();
                    tmdbMoviesList.addAll(response.getResults());
                    tmdbAdapter.updateMovies(tmdbMoviesList);

                    resultCountText.setText("Phim phổ biến (" + response.getResults().size() + " phim)");
                    resultCountText.setVisibility(View.VISIBLE);
                } else {
                    showNoResults();
                }
            }

            @Override
            public void onError(String error) {
                isLoading = false;
                showLoading(false);
                showNoResults();
                Toast.makeText(ManageMoviesActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMovieToSystem(TMDbMovie movie) {
        // Kiểm tra xem phim đã tồn tại chưa
        checkMovieExists(movie, exists -> {
            if (exists) {
                Toast.makeText(this, "Phim \"" + movie.getTitle() + "\" đã tồn tại trong hệ thống!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hiển thị loading
            showLoading(true);

            // Lấy video từ API /movie/{id}/videos
            apiService.getMovieVideos(movie.getId(), new TMDbApiService.VideoCallback() {
                @Override
                public void onSuccess(TMDbVideoResponse videoResponse) {
                    showLoading(false);

                    // Tìm trailer YouTube đầu tiên
                    TMDbVideo trailer = videoResponse.getFirstYouTubeTrailer();
                    if (trailer != null) {
                        movie.setYoutubeKey(trailer.getKey());
                        Toast.makeText(ManageMoviesActivity.this, "✓ Đã tìm thấy trailer: " + trailer.getName(), Toast.LENGTH_SHORT).show();
                    } else {
                        // Tìm video YouTube bất kỳ nếu không có trailer
                        TMDbVideo anyVideo = videoResponse.getFirstYouTubeVideo();
                        if (anyVideo != null) {
                            movie.setYoutubeKey(anyVideo.getKey());
                            Toast.makeText(ManageMoviesActivity.this, "✓ Đã tìm thấy video: " + anyVideo.getName(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ManageMoviesActivity.this, "⚠ Không tìm thấy video, bạn có thể nhập URL thủ công", Toast.LENGTH_SHORT).show();
                        }
                    }

                    // Chuyển sang AddMovieDetailsActivity
                    Intent intent = new Intent(ManageMoviesActivity.this, AddMovieDetailsActivity.class);
                    intent.putExtra("item_tmdb_movie", movie);
                    startActivityForResult(intent, REQUEST_ADD_MOVIE);
                }

                @Override
                public void onError(String error) {
                    showLoading(false);
                    Toast.makeText(ManageMoviesActivity.this, "⚠ Không thể lấy video: " + error, Toast.LENGTH_SHORT).show();

                    // Vẫn chuyển sang AddMovieDetailsActivity nhưng không có video
                    Intent intent = new Intent(ManageMoviesActivity.this, AddMovieDetailsActivity.class);
                    intent.putExtra("item_tmdb_movie", movie);
                    startActivityForResult(intent, REQUEST_ADD_MOVIE);
                }
            });
        });
    }

    private void checkMovieExists(TMDbMovie movie, OnMovieExistsCallback callback) {
        executorService.execute(() -> {
            try {
                List<Movie> existingMovies = databaseHelper.movieDao().searchMoviesByTitle(movie.getTitle());
                boolean exists = !existingMovies.isEmpty();

                runOnUiThread(() -> callback.onResult(exists));
            } catch (Exception e) {
                runOnUiThread(() -> callback.onResult(false));
            }
        });
    }

    private interface OnMovieExistsCallback {
        void onResult(boolean exists);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ADD_MOVIE && data.getBooleanExtra("movie_added", false)) {
                String movieTitle = data.getStringExtra("movie_title");
                Toast.makeText(this, "Đã thêm phim: " + movieTitle, Toast.LENGTH_SHORT).show();

                // Refresh local movies if currently showing
                if (showingLocalMovies) {
                    loadLocalMovies();
                }
            } else if (requestCode == REQUEST_EDIT_MOVIE) {
                if (data.getBooleanExtra("movie_updated", false)) {
                    String movieTitle = data.getStringExtra("movie_title");
                    Toast.makeText(this, "Đã cập nhật phim: " + movieTitle, Toast.LENGTH_SHORT).show();
                } else if (data.getBooleanExtra("movie_deleted", false)) {
                    String movieTitle = data.getStringExtra("movie_title");
                    Toast.makeText(this, "Đã xóa phim: " + movieTitle, Toast.LENGTH_SHORT).show();
                }

                // Refresh local movies
                if (showingLocalMovies) {
                    loadLocalMovies();
                }
            }
        }
    }

    private void setupRecyclerView() {
        // Setup TMDb adapter
        tmdbAdapter = new TMDbMovieAdapter(this, tmdbMoviesList);
        tmdbAdapter.setOnMovieClickListener(this);

        // Setup Local adapter
        localAdapter = new LocalMovieAdapter(this, localMoviesList);
        localAdapter.setOnLocalMovieClickListener(this);

        moviesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        moviesRecyclerView.setAdapter(localAdapter); // Default to local adapter
    }

    private void setupSearchListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (showingLocalMovies) {
                    if (s.length() > 0) {
                        searchLocalMovies(s.toString());
                    } else {
                        loadLocalMovies();
                    }
                } else {
                    if (s.length() > 2) {
                        searchMovies(s.toString());
                    } else if (s.length() == 0) {
                        loadPopularMovies();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchMovies(String query) {
        if (isLoading) return;

        isLoading = true;
        currentPage = 1;
        showLoading(true);
        hideNoResults();

        apiService.searchMovies(query, currentPage, new TMDbApiService.SearchCallback() {
            @Override
            public void onSuccess(TMDbSearchResponse response) {
                isLoading = false;
                showLoading(false);

                if (response.getResults() != null && !response.getResults().isEmpty()) {
                    tmdbMoviesList.clear();
                    tmdbMoviesList.addAll(response.getResults());
                    tmdbAdapter.updateMovies(tmdbMoviesList);

                    resultCountText.setText("Tìm thấy " + response.getTotalResults() + " kết quả cho \"" + query + "\"");
                    resultCountText.setVisibility(View.VISIBLE);
                } else {
                    showNoResults();
                }
            }

            @Override
            public void onError(String error) {
                isLoading = false;
                showLoading(false);
                showNoResults();
                Toast.makeText(ManageMoviesActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearResults() {
        if (showingLocalMovies) {
            localMoviesList.clear();
            localAdapter.updateMovies(localMoviesList);
        } else {
            tmdbMoviesList.clear();
            tmdbAdapter.updateMovies(tmdbMoviesList);
        }
        resultCountText.setVisibility(View.GONE);
        hideNoResults();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showNoResults() {
        noResultsText.setVisibility(View.VISIBLE);
        resultCountText.setVisibility(View.GONE);
    }

    private void hideNoResults() {
        noResultsText.setVisibility(View.GONE);
    }

    // TMDb Movie Click Handlers
    @Override
    public void onMovieClick(TMDbMovie movie) {
        showMovieDetails(movie);
    }

    @Override
    public void onAddMovieClick(TMDbMovie movie) {
        openAddMovieScreen(movie);
    }

    // Local Movie Click Handlers
    @Override
    public void onLocalMovieClick(Movie movie) {
        // View details of local movie
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("object", movie);
        startActivity(intent);
    }

    @Override
    public void onEditMovieClick(Movie movie) {
        // Edit local movie
        Intent intent = new Intent(this, EditMovieActivity.class);
        intent.putExtra("movie_id", movie.getId());
        startActivityForResult(intent, REQUEST_EDIT_MOVIE);
    }

    @Override
    public void onDeleteMovieClick(Movie movie) {
        // Confirm delete
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa phim \"" + movie.getTitle() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteLocalMovie(movie))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteLocalMovie(Movie movie) {
        showLoading(true);

        executorService.execute(() -> {
            try {
                // Xóa MovieDetail trước nếu có
                databaseHelper.movieDao().deleteMovieDetailById(movie.getId());
                // Xóa Movie
                databaseHelper.movieDao().deleteMovie(movie);

                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Đã xóa phim: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
                    loadLocalMovies(); // Refresh list
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Lỗi khi xóa phim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showMovieDetails(TMDbMovie movie) {
        Toast.makeText(this, "Chi tiết: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void openAddMovieScreen(TMDbMovie movie) {
        addMovieToSystem(movie);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}