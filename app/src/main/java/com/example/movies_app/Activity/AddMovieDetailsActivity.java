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
import com.example.movies_app.Domain.FilmItem;
import com.example.movies_app.Domain.TMDbMovie;
import com.example.movies_app.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
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
    private Button btnAddMovie, btnCancel;
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

    // Override method abstract từ BaseActivity
    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_add_movie_details;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Không cần setContentView() nữa vì BaseActivity đã handle

        initViews();
        setupData();
        setupGenreChips();
        setupClickListeners();
    }

    // Override method từ BaseActivity
    @Override
    protected String getCurrentTab() {
        return ""; // Hoặc return tab name phù hợp nếu activity này có tab
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
        progressBar = findViewById(R.id.progressBar);

        // Sử dụng AppDatabase singleton
        databaseHelper = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
    }

    private void setupData() {
        // Lấy dữ liệu TMDb từ Intent
        Intent intent = getIntent();
        if (intent.hasExtra("tmdb_movie")) {
            tmdbMovie = (TMDbMovie) intent.getSerializableExtra("tmdb_movie");
            if (tmdbMovie != null) {
                displayTmdbInfo();
                prefillData();
            }
        }
    }

    private void displayTmdbInfo() {
        // Hiển thị thông tin từ TMDb
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
        // Điền sẵn một số thông tin từ TMDb
        edtTitle.setText(tmdbMovie.getTitle());
        edtPlot.setText(tmdbMovie.getOverview());

        if (!TextUtils.isEmpty(tmdbMovie.getReleaseDate())) {
            String year = tmdbMovie.getReleaseDate().substring(0, 4);
            edtYear.setText(year);
        }

        edtImdbRating.setText(String.valueOf(tmdbMovie.getVoteAverage()));
        edtImdbVotes.setText(String.valueOf(tmdbMovie.getVoteCount()));
    }

    private void setupGenreChips() {
        chipGroupGenres.removeAllViews();

        for (String genre : commonGenres) {
            Chip chip = new Chip(this);
            chip.setText(genre);
            chip.setCheckable(true);

            // Sửa lỗi color selector - sử dụng programmatic color setting
            chip.setChipBackgroundColor(ContextCompat.getColorStateList(this, android.R.color.transparent));
            chip.setTextColor(ContextCompat.getColorStateList(this, android.R.color.black));

            // Hoặc bạn có thể tạo color selector programmatically
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

        // Cập nhật EditText genres khi chọn chips
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

        // Tạo FilmItem từ dữ liệu nhập vào
        FilmItem filmItem = createMovieFromForm();

        // Sử dụng background thread để thêm vào database
        executorService.execute(() -> {
            try {
                // Tạm thời lưu FilmItem vào một bảng khác hoặc convert sang Movie
                // Bạn cần tạo method để convert FilmItem -> Movie hoặc tạo DAO cho FilmItem

                // GIẢI PHÁP TẠM: Lưu thông tin cơ bản vào Movie với constructor
                // Cần biết chính xác constructor của Movie để điều chỉnh

                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Chức năng đang được phát triển. Movie entity cần được cấu hình đúng constructor.", Toast.LENGTH_LONG).show();
                    finish();
                });

                /*
                // Code này sẽ hoạt động khi bạn cung cấp đúng Movie constructor:
                Movie movie = new Movie(
                    // Cần điền đúng tham số theo constructor: int,String,String,String,String,String,String,String,String,int
                    0, // id
                    filmItem.getTitle(),
                    filmItem.getYear(),
                    filmItem.getDirector(),
                    filmItem.getGenres().toString(),
                    filmItem.getPoster(),
                    filmItem.getImdbRating(),
                    filmItem.getPlot(),
                    filmItem.getActors(),
                    Integer.parseInt(filmItem.getImdbVotes().replaceAll("[^0-9]", ""))
                );

                databaseHelper.movieDao().insertMovie(movie);

                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Thêm phim thành công!", Toast.LENGTH_SHORT).show();

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("movie_added", true);
                    resultIntent.putExtra("movie_title", movie.getTitle());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                });
                */

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Lỗi khi thêm phim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
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

    private Movie createMovieEntity() {
        // Movie constructor yêu cầu parameters - cần điều chỉnh theo constructor thực tế
        // Tạm thời sử dụng FilmItem thay vì Movie vì Movie constructor phức tạp
        return null; // Sẽ sửa lại bên dưới
    }

    // Sử dụng FilmItem thay vì Movie entity vì Movie constructor phức tạp
    private FilmItem createMovieFromForm() {
        FilmItem filmItem = new FilmItem();

        filmItem.setTitle(edtTitle.getText().toString().trim());
        filmItem.setYear(edtYear.getText().toString().trim());
        filmItem.setRated(edtRated.getText().toString().trim());
        filmItem.setReleased(tmdbMovie.getReleaseDate());
        filmItem.setRuntime(edtRuntime.getText().toString().trim());
        filmItem.setDirector(edtDirector.getText().toString().trim());
        filmItem.setWriter(edtWriter.getText().toString().trim());
        filmItem.setActors(edtActors.getText().toString().trim());
        filmItem.setPlot(edtPlot.getText().toString().trim());
        filmItem.setCountry(edtCountry.getText().toString().trim());
        filmItem.setAwards(edtAwards.getText().toString().trim());
        filmItem.setMetascore(edtMetascore.getText().toString().trim());
        filmItem.setImdbRating(edtImdbRating.getText().toString().trim());
        filmItem.setImdbVotes(edtImdbVotes.getText().toString().trim());
        filmItem.setImdbId(edtImdbId.getText().toString().trim());
        filmItem.setType("movie");

        // Set poster từ TMDb
        filmItem.setPoster(tmdbMovie.getFullPosterUrl());

        // Xử lý genres
        String genresText = edtGenres.getText().toString().trim();
        List<String> genresList = Arrays.asList(genresText.split(",\\s*"));
        filmItem.setGenres(genresList);

        // Set images (poster và backdrop)
        List<String> images = new ArrayList<>();
        images.add(tmdbMovie.getFullPosterUrl());
        if (!TextUtils.isEmpty(tmdbMovie.getFullBackdropUrl())) {
            images.add(tmdbMovie.getFullBackdropUrl());
        }
        filmItem.setImages(images);

        return filmItem;
    }

    // Giữ lại method createFilmItem nếu cần thiết cho các mục đích khác
    private FilmItem createFilmItem() {
        FilmItem filmItem = new FilmItem();

        filmItem.setTitle(edtTitle.getText().toString().trim());
        filmItem.setYear(edtYear.getText().toString().trim());
        filmItem.setRated(edtRated.getText().toString().trim());
        filmItem.setReleased(tmdbMovie.getReleaseDate());
        filmItem.setRuntime(edtRuntime.getText().toString().trim());
        filmItem.setDirector(edtDirector.getText().toString().trim());
        filmItem.setWriter(edtWriter.getText().toString().trim());
        filmItem.setActors(edtActors.getText().toString().trim());
        filmItem.setPlot(edtPlot.getText().toString().trim());
        filmItem.setCountry(edtCountry.getText().toString().trim());
        filmItem.setAwards(edtAwards.getText().toString().trim());
        filmItem.setMetascore(edtMetascore.getText().toString().trim());
        filmItem.setImdbRating(edtImdbRating.getText().toString().trim());
        filmItem.setImdbVotes(edtImdbVotes.getText().toString().trim());
        filmItem.setImdbId(edtImdbId.getText().toString().trim());
        filmItem.setType("movie");

        // Set poster từ TMDb
        filmItem.setPoster(tmdbMovie.getFullPosterUrl());

        // Xử lý genres
        String genresText = edtGenres.getText().toString().trim();
        List<String> genresList = Arrays.asList(genresText.split(",\\s*"));
        filmItem.setGenres(genresList);

        // Set images (poster và backdrop)
        List<String> images = new ArrayList<>();
        images.add(tmdbMovie.getFullPosterUrl());
        if (!TextUtils.isEmpty(tmdbMovie.getFullBackdropUrl())) {
            images.add(tmdbMovie.getFullBackdropUrl());
        }
        filmItem.setImages(images);

        return filmItem;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnAddMovie.setEnabled(!show);
        btnCancel.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}