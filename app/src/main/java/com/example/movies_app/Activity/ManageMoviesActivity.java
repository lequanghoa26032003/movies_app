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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movies_app.Adapter.TMDbMovieAdapter;
import com.example.movies_app.Domain.TMDbMovie;
import com.example.movies_app.Domain.TMDbSearchResponse;
import com.example.movies_app.Domain.TMDbVideoResponse;
import com.example.movies_app.Domain.TMDbVideo;
import com.example.movies_app.Helper.TMDbApiService;
import com.example.movies_app.R;

import java.util.ArrayList;
import java.util.List;

public class ManageMoviesActivity extends AppCompatActivity implements TMDbMovieAdapter.OnMovieClickListener {

    private EditText searchEditText;
    private RecyclerView moviesRecyclerView;
    private ProgressBar progressBar;
    private TextView resultCountText, noResultsText;

    private TMDbApiService apiService;
    private TMDbMovieAdapter adapter;
    private List<TMDbMovie> moviesList;
    private static final int REQUEST_ADD_MOVIE = 1001;
    private int currentPage = 1;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_movies);

        initViews();
        setupRecyclerView();
        setupSearchListener();

        apiService = new TMDbApiService(this);
        moviesList = new ArrayList<>();

        // Load popular movies by default
        loadPopularMovies();
    }

    private void initViews() {
        searchEditText = findViewById(R.id.movieSearchEditText);
        moviesRecyclerView = findViewById(R.id.moviesRecyclerView);
        progressBar = findViewById(R.id.searchProgressBar);
        resultCountText = findViewById(R.id.resultCountText);
        noResultsText = findViewById(R.id.noResultsText);
    }

    // Load popular movies when app starts
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
                    moviesList.clear();
                    moviesList.addAll(response.getResults());
                    adapter.updateMovies(moviesList);

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_MOVIE && resultCode == RESULT_OK) {
            if (data.getBooleanExtra("movie_added", false)) {
                String movieTitle = data.getStringExtra("movie_title");
                Toast.makeText(this, "Đã thêm phim: " + movieTitle, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupRecyclerView() {
        adapter = new TMDbMovieAdapter(this, moviesList);
        adapter.setOnMovieClickListener(this);
        moviesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        moviesRecyclerView.setAdapter(adapter);
    }

    private void setupSearchListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    searchMovies(s.toString());
                } else if (s.length() == 0) {
                    // Khi search box trống, load lại popular movies
                    loadPopularMovies();
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
                    moviesList.clear();
                    moviesList.addAll(response.getResults());
                    adapter.updateMovies(moviesList);

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
        moviesList.clear();
        adapter.updateMovies(moviesList);
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

    @Override
    public void onMovieClick(TMDbMovie movie) {
        showMovieDetails(movie);
    }

    @Override
    public void onAddMovieClick(TMDbMovie movie) {
        openAddMovieScreen(movie);
    }

    private void showMovieDetails(TMDbMovie movie) {
        Toast.makeText(this, "Chi tiết: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void openAddMovieScreen(TMDbMovie movie) {
        addMovieToSystem(movie);
    }
}