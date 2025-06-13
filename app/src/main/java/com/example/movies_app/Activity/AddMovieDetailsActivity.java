package com.example.movies_app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.movies_app.Database.AppDatabase;
import com.example.movies_app.Database.entity.Movie;
import com.example.movies_app.Database.entity.MovieDetail;
import com.example.movies_app.Domain.TMDbMovie;
import com.example.movies_app.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddMovieDetailsActivity extends BaseActivity {

    private ImageView imgPoster, imgBackdrop;
    private TextView txtTmdbTitle, txtTmdbOverview, txtTmdbReleaseDate, txtTmdbRating;
    private EditText edtTitle, edtYear, edtRated, edtRuntime, edtDirector, edtWriter,
            edtActors, edtPlot, edtCountry, edtAwards, edtMetascore,
            edtImdbRating, edtImdbVotes, edtImdbId, edtGenres, edtVideoUrl;
    private ChipGroup chipGroupGenres;
    private Button btnAddMovie, btnCancel, btnViewDetails;
    private ProgressBar progressBar;

    private TMDbMovie tmdbMovie;
    private AppDatabase databaseHelper;
    private ExecutorService executorService;

    // Danh sách thể loại phổ biến
    private String[] commonGenres = {
            "Action", "Adventure", "Animation", "Biography", "Comedy", "Crime",
            "Documentary", "Drama", "Family", "Fantasy", "History", "Horror",
            "Music", "Mystery", "Romance", "Science Fiction", "Thriller", "War", "Western"
    };

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_add_movie_details;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initViews();
        setupData();
        setupGenreChips();
        setupClickListeners();
    }

    @Override
    protected String getCurrentTab() {
        return "";
    }

    private void initViews() {
        // TMDb info views
        imgPoster = findViewById(R.id.imgPoster);
        imgBackdrop = findViewById(R.id.imgBackdrop);
        txtTmdbTitle = findViewById(R.id.txtTmdbTitle);
        txtTmdbOverview = findViewById(R.id.txtTmdbOverview);
        txtTmdbReleaseDate = findViewById(R.id.txtTmdbReleaseDate);
        txtTmdbRating = findViewById(R.id.txtTmdbRating);

        // Edit fields
        edtTitle = findViewById(R.id.edtTitle);
        edtYear = findViewById(R.id.edtYear);
        edtRated = findViewById(R.id.edtRated);
        edtRuntime = findViewById(R.id.edtRuntime);
        edtDirector = findViewById(R.id.edtDirector);
        edtWriter = findViewById(R.id.edtWriter);
        edtActors = findViewById(R.id.edtActors);
        edtPlot = findViewById(R.id.edtPlot);
        edtCountry = findViewById(R.id.edtCountry);
        edtAwards = findViewById(R.id.edtAwards);
        edtMetascore = findViewById(R.id.edtMetascore);
        edtImdbRating = findViewById(R.id.edtImdbRating);
        edtImdbVotes = findViewById(R.id.edtImdbVotes);
        edtImdbId = findViewById(R.id.edtImdbId);
        edtGenres = findViewById(R.id.edtGenres);
        edtVideoUrl = findViewById(R.id.edtVideoUrl);

        chipGroupGenres = findViewById(R.id.chipGroupGenres);
        btnAddMovie = findViewById(R.id.btnAddMovie);
        btnCancel = findViewById(R.id.btnCancel);
        btnViewDetails = findViewById(R.id.btnViewDetails); // Nút xem chi tiết
        progressBar = findViewById(R.id.progressBar);

        databaseHelper = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
    }

    private void setupData() {
        Intent intent = getIntent();
        if (intent.hasExtra("item_tmdb_movie")) {
            tmdbMovie = (TMDbMovie) intent.getSerializableExtra("item_tmdb_movie");
            if (tmdbMovie != null) {
                displayTmdbInfo();
                prefillData();
            }
        }
    }

    private void displayTmdbInfo() {
        txtTmdbTitle.setText(tmdbMovie.getTitle());
        txtTmdbOverview.setText(tmdbMovie.getOverview());
        txtTmdbReleaseDate.setText("Ngày phát hành: " + tmdbMovie.getReleaseDate());
        txtTmdbRating.setText(String.format("Đánh giá: %.1f/10 (%d votes)",
                tmdbMovie.getVoteAverage(), tmdbMovie.getVoteCount()));

        // Load hình ảnh
        if (!TextUtils.isEmpty(tmdbMovie.getFullPosterUrl())) {
            Glide.with(this)
                    .load(tmdbMovie.getFullPosterUrl())
                    .into(imgPoster);
        }

        if (!TextUtils.isEmpty(tmdbMovie.getFullBackdropUrl())) {
            Glide.with(this)
                    .load(tmdbMovie.getFullBackdropUrl())
                    .into(imgBackdrop);
        }
    }

    private void prefillData() {
        edtTitle.setText(tmdbMovie.getTitle());
        edtPlot.setText(tmdbMovie.getOverview());

        if (!TextUtils.isEmpty(tmdbMovie.getReleaseDate())) {
            String year = tmdbMovie.getReleaseDate().substring(0, 4);
            edtYear.setText(year);
        }

        edtImdbRating.setText(String.valueOf(tmdbMovie.getVoteAverage()));
        edtImdbVotes.setText(String.valueOf(tmdbMovie.getVoteCount()));

        // Tự động điền URL video nếu có trailer từ TMDB
        if (tmdbMovie.getVideoUrl() != null && !tmdbMovie.getVideoUrl().isEmpty()) {
            edtVideoUrl.setText(tmdbMovie.getVideoUrl());

            // Hiển thị thông báo đã tìm thấy trailer
            Toast.makeText(this, "✓ Đã tìm thấy trailer từ TMDB", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupGenreChips() {
        chipGroupGenres.removeAllViews();

        for (String genre : commonGenres) {
            Chip chip = new Chip(this);
            chip.setText(genre);
            chip.setCheckable(true);

            chip.setChipBackgroundColor(ContextCompat.getColorStateList(this, android.R.color.transparent));
            chip.setTextColor(ContextCompat.getColorStateList(this, android.R.color.black));

            chip.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                if (isChecked) {
                    chip.setChipBackgroundColor(ContextCompat.getColorStateList(this, R.color.purple_200));
                    chip.setTextColor(ContextCompat.getColorStateList(this, android.R.color.white));
                } else {
                    chip.setChipBackgroundColor(ContextCompat.getColorStateList(this, android.R.color.transparent));
                    chip.setTextColor(ContextCompat.getColorStateList(this, android.R.color.black));
                }
            });

            chipGroupGenres.addView(chip);
        }
    }

    private void setupClickListeners() {
        btnAddMovie.setOnClickListener(v -> addMovieToSystem());
        btnCancel.setOnClickListener(v -> finish());
        btnViewDetails.setOnClickListener(v -> viewMovieDetails());

        chipGroupGenres.setOnCheckedStateChangeListener((group, checkedIds) -> {
            List<String> selectedGenres = new ArrayList<>();
            for (int id : checkedIds) {
                Chip chip = findViewById(id);
                if (chip != null) {
                    selectedGenres.add(chip.getText().toString());
                }
            }
            edtGenres.setText(TextUtils.join(", ", selectedGenres));
        });
    }

    private void addMovieToSystem() {
        if (!validateInput()) {
            return;
        }

        showLoading(true);

        // Cập nhật thông tin vào TMDbMovie object
        updateTMDbMovieWithFormData();

        // Lưu vào database
        executorService.execute(() -> {
            try {
                // Tạo Movie entity từ TMDbMovie
                Movie movie = convertTMDbMovieToMovie(tmdbMovie);

                // Lưu movie vào database
                databaseHelper.movieDao().insertMovie(movie);

                // Tạo MovieDetail và lưu vào database
                MovieDetail movieDetail = createMovieDetail(tmdbMovie, movie.getId());
                databaseHelper.movieDao().insertMovieDetail(movieDetail);

                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Thêm phim thành công vào database!", Toast.LENGTH_SHORT).show();

                    // Mark as added to system
                    tmdbMovie.setAddedToSystem(true);

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("movie_added", true);
                    resultIntent.putExtra("movie_title", tmdbMovie.getTitle());
                    resultIntent.putExtra("updated_movie", tmdbMovie);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Lỗi khi thêm phim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    private Movie convertTMDbMovieToMovie(TMDbMovie tmdbMovie) {
        // Tạo ID duy nhất cho movie (có thể dùng timestamp hoặc random)
        int movieId = (int) System.currentTimeMillis();

        String title = edtTitle.getText().toString().trim();
        String poster = tmdbMovie.getFullPosterUrl();
        String year = edtYear.getText().toString().trim();
        String country = edtCountry.getText().toString().trim();
        String imdbRating = edtImdbRating.getText().toString().trim();
        String genres = edtGenres.getText().toString().trim();
        String images = tmdbMovie.getFullBackdropUrl(); // Sử dụng backdrop URL cho images
        String lastUpdated = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
        int isDownloaded = 0; // Mặc định chưa download

        return new Movie(movieId, title, poster, year, country, imdbRating,
                genres, images, lastUpdated, isDownloaded);
    }

    // Thêm method helper để tạo MovieDetail
    private MovieDetail createMovieDetail(TMDbMovie tmdbMovie, int movieId) {
        String released = tmdbMovie.getReleaseDate();
        String runtime = edtRuntime.getText().toString().trim();
        String director = edtDirector.getText().toString().trim();
        String writer = edtWriter.getText().toString().trim();
        String actors = edtActors.getText().toString().trim();
        String plot = edtPlot.getText().toString().trim();
        String awards = edtAwards.getText().toString().trim();
        String metascore = edtMetascore.getText().toString().trim();
        String imdbVotes = edtImdbVotes.getText().toString().trim();
        String type = "movie"; // Mặc định là movie
        String videoUrl = edtVideoUrl.getText().toString().trim();
        String subtitleUrl = ""; // Để trống vì chưa có
        String lastUpdated = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());

        return new MovieDetail(movieId, released, runtime, director, writer, actors,
                plot, awards, metascore, imdbVotes, type, videoUrl,
                subtitleUrl, lastUpdated);
    }

    private void viewMovieDetails() {
        if (!validateInput()) {
            return;
        }

        // Cập nhật thông tin vào TMDbMovie object
        updateTMDbMovieWithFormData();

        // Chuyển đến DetailActivity với dữ liệu đầy đủ
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("tmdb_movie", tmdbMovie);
        intent.putExtra("use_tmdb_data", true); // Flag để DetailActivity biết dùng dữ liệu TMDb
        startActivity(intent);
    }

    private void updateTMDbMovieWithFormData() {
        // Cập nhật tất cả thông tin từ form vào TMDbMovie object
        tmdbMovie.setVideoUrl(edtVideoUrl.getText().toString().trim());
        tmdbMovie.setDirector(edtDirector.getText().toString().trim());
        tmdbMovie.setWriter(edtWriter.getText().toString().trim());
        tmdbMovie.setActors(edtActors.getText().toString().trim());
        tmdbMovie.setCountry(edtCountry.getText().toString().trim());
        tmdbMovie.setAwards(edtAwards.getText().toString().trim());
        tmdbMovie.setRuntime(edtRuntime.getText().toString().trim());
        tmdbMovie.setRated(edtRated.getText().toString().trim());
        tmdbMovie.setImdbId(edtImdbId.getText().toString().trim());
        tmdbMovie.setGenresText(edtGenres.getText().toString().trim());

        // Cập nhật title và plot nếu đã chỉnh sửa
        tmdbMovie.setTitle(edtTitle.getText().toString().trim());
        tmdbMovie.setOverview(edtPlot.getText().toString().trim());
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(edtTitle.getText().toString().trim())) {
            edtTitle.setError("Vui lòng nhập tên phim");
            edtTitle.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(edtYear.getText().toString().trim())) {
            edtYear.setError("Vui lòng nhập năm phát hành");
            edtYear.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(edtDirector.getText().toString().trim())) {
            edtDirector.setError("Vui lòng nhập tên đạo diễn");
            edtDirector.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(edtGenres.getText().toString().trim())) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một thể loại", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(edtVideoUrl.getText().toString().trim())) {
            edtVideoUrl.setError("Vui lòng nhập URL video");
            edtVideoUrl.requestFocus();
            return false;
        }

        return true;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnAddMovie.setEnabled(!show);
        btnCancel.setEnabled(!show);
        btnViewDetails.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}