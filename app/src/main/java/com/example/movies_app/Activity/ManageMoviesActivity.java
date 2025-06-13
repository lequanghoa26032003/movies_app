package com.example.movies_app.Activity;

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
    }

    private void initViews() {
        searchEditText = findViewById(R.id.movieSearchEditText);
        moviesRecyclerView = findViewById(R.id.moviesRecyclerView);
        progressBar = findViewById(R.id.searchProgressBar);
        resultCountText = findViewById(R.id.resultCountText);
        noResultsText = findViewById(R.id.noResultsText);
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
                if (s.length() > 2) { // Tìm kiếm khi có ít nhất 3 ký tự
                    searchMovies(s.toString());
                } else {
                    clearResults();
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

                    resultCountText.setText("Tìm thấy " + response.getTotalResults() + " kết quả");
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
        // Hiển thị chi tiết phim
        showMovieDetails(movie);
    }

    @Override
    public void onAddMovieClick(TMDbMovie movie) {
        // Chuyển đến màn hình thêm phim với thông tin từ TMDb
        openAddMovieScreen(movie);
    }

    private void showMovieDetails(TMDbMovie movie) {
        // TODO: Hiển thị dialog hoặc fragment với chi tiết phim
        Toast.makeText(this, "Chi tiết: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void openAddMovieScreen(TMDbMovie movie) {
        // TODO: Mở màn hình thêm phim với dữ liệu từ TMDb
        Toast.makeText(this, "Thêm phim: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
    }
}