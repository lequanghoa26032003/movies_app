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
import com.example.movies_app.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditMovieActivity extends BaseActivity {

    private ImageView imgPoster, imgBackdrop;
    private TextView txtTitle;
    private EditText edtTitle, edtYear, edtRated, edtRuntime, edtDirector, edtWriter,
            edtActors, edtPlot, edtCountry, edtAwards, edtMetascore,
            edtImdbRating, edtImdbVotes, edtImdbId, edtGenres, edtVideoUrl;
    private ChipGroup chipGroupGenres;
    private Button btnUpdateMovie, btnCancel, btnViewDetails, btnDeleteMovie;
    private ProgressBar progressBar;

    private Movie currentMovie;
    private MovieDetail currentMovieDetail;
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
        return R.layout.activity_edit_movie;
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
        imgPoster = findViewById(R.id.imgPoster);
        imgBackdrop = findViewById(R.id.imgBackdrop);
        txtTitle = findViewById(R.id.txtTitle);

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
        btnUpdateMovie = findViewById(R.id.btnUpdateMovie);
        btnCancel = findViewById(R.id.btnCancel);
        btnViewDetails = findViewById(R.id.btnViewDetails);
        btnDeleteMovie = findViewById(R.id.btnDeleteMovie);
        progressBar = findViewById(R.id.progressBar);

        databaseHelper = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
    }

    private void setupData() {
        Intent intent = getIntent();
        int movieId = intent.getIntExtra("movie_id", -1);

        if (movieId != -1) {
            loadMovieData(movieId);
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin phim", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadMovieData(int movieId) {
        showLoading(true);

        executorService.execute(() -> {
            try {
                currentMovie = databaseHelper.movieDao().getMovieById(movieId);
                currentMovieDetail = databaseHelper.movieDao().getMovieDetailById(movieId);

                runOnUiThread(() -> {
                    showLoading(false);
                    if (currentMovie != null) {
                        displayMovieInfo();
                        prefillData();
                    } else {
                        Toast.makeText(this, "Không tìm thấy phim", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void displayMovieInfo() {
        txtTitle.setText("Chỉnh sửa: " + currentMovie.getTitle());

        // Load hình ảnh
        if (!TextUtils.isEmpty(currentMovie.getPoster())) {
            Glide.with(this)
                    .load(currentMovie.getPoster())
                    .into(imgPoster);
        }

        if (!TextUtils.isEmpty(currentMovie.getImages())) {
            Glide.with(this)
                    .load(currentMovie.getImages())
                    .into(imgBackdrop);
        }
    }

    private void prefillData() {
        edtTitle.setText(currentMovie.getTitle());
        edtYear.setText(currentMovie.getYear());
        edtCountry.setText(currentMovie.getCountry());
        edtImdbRating.setText(currentMovie.getImdbRating());
        edtGenres.setText(currentMovie.getGenres());

        if (currentMovieDetail != null) {
            edtRated.setText(currentMovieDetail.getRated());
            edtRuntime.setText(currentMovieDetail.getRuntime());
            edtDirector.setText(currentMovieDetail.getDirector());
            edtWriter.setText(currentMovieDetail.getWriter());
            edtActors.setText(currentMovieDetail.getActors());
            edtPlot.setText(currentMovieDetail.getPlot());
            edtAwards.setText(currentMovieDetail.getAwards());
            edtMetascore.setText(currentMovieDetail.getMetascore());
            edtImdbVotes.setText(currentMovieDetail.getImdbVotes());
            edtVideoUrl.setText(currentMovieDetail.getVideoUrl());
        }

        // Set selected genres
        setSelectedGenres();
    }

    private void setSelectedGenres() {
        if (currentMovie.getGenres() != null) {
            String[] selectedGenres = currentMovie.getGenres().split(", ");
            for (int i = 0; i < chipGroupGenres.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupGenres.getChildAt(i);
                String chipText = chip.getText().toString();

                for (String genre : selectedGenres) {
                    if (chipText.equals(genre.trim())) {
                        chip.setChecked(true);
                        break;
                    }
                }
            }
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
        btnUpdateMovie.setOnClickListener(v -> updateMovie());
        btnCancel.setOnClickListener(v -> finish());
        btnViewDetails.setOnClickListener(v -> viewMovieDetails());
        btnDeleteMovie.setOnClickListener(v -> confirmDeleteMovie());

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

    private void updateMovie() {
        if (!validateInput()) {
            return;
        }

        showLoading(true);

        executorService.execute(() -> {
            try {
                // Cập nhật Movie
                currentMovie.setTitle(edtTitle.getText().toString().trim());
                currentMovie.setYear(edtYear.getText().toString().trim());
                currentMovie.setCountry(edtCountry.getText().toString().trim());
                currentMovie.setImdbRating(edtImdbRating.getText().toString().trim());
                currentMovie.setGenres(edtGenres.getText().toString().trim());
                currentMovie.setLastUpdated(java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()));

                databaseHelper.movieDao().updateMovie(currentMovie);

                // Cập nhật MovieDetail
                if (currentMovieDetail == null) {
                    currentMovieDetail = new MovieDetail();
                    currentMovieDetail.setId(currentMovie.getId());
                }

                currentMovieDetail.setRuntime(edtRuntime.getText().toString().trim());
                currentMovieDetail.setDirector(edtDirector.getText().toString().trim());
                currentMovieDetail.setWriter(edtWriter.getText().toString().trim());
                currentMovieDetail.setActors(edtActors.getText().toString().trim());
                currentMovieDetail.setPlot(edtPlot.getText().toString().trim());
                currentMovieDetail.setAwards(edtAwards.getText().toString().trim());
                currentMovieDetail.setMetascore(edtMetascore.getText().toString().trim());
                currentMovieDetail.setImdbVotes(edtImdbVotes.getText().toString().trim());
                currentMovieDetail.setVideoUrl(edtVideoUrl.getText().toString().trim());
                currentMovieDetail.setRated(edtRated.getText().toString().trim());
                currentMovieDetail.setLastUpdated(java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()));

                if (currentMovieDetail.getMovieId() == 0) {
                    // Insert new MovieDetail
                    currentMovieDetail.setMovieId(currentMovie.getId());
                    databaseHelper.movieDao().insertMovieDetail(currentMovieDetail);
                } else {
                    // Update existing MovieDetail
                    databaseHelper.movieDao().updateMovieDetail(currentMovieDetail);
                }

                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Cập nhật phim thành công!", Toast.LENGTH_SHORT).show();

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("movie_updated", true);
                    resultIntent.putExtra("movie_title", currentMovie.getTitle());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Lỗi khi cập nhật phim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void confirmDeleteMovie() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa phim \"" + currentMovie.getTitle() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteMovie())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteMovie() {
        showLoading(true);

        executorService.execute(() -> {
            try {
                // Xóa MovieDetail trước
                if (currentMovieDetail != null) {
                    databaseHelper.movieDao().deleteMovieDetail(currentMovieDetail);
                }

                // Xóa Movie
                databaseHelper.movieDao().deleteMovie(currentMovie);

                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Đã xóa phim: " + currentMovie.getTitle(), Toast.LENGTH_SHORT).show();

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("movie_deleted", true);
                    resultIntent.putExtra("movie_title", currentMovie.getTitle());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Lỗi khi xóa phim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void viewMovieDetails() {
        if (!validateInput()) {
            return;
        }

        // Tạm thời cập nhật dữ liệu để xem preview
        updateMovieTemporary();

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("object", currentMovie);
        startActivity(intent);
    }

    private void updateMovieTemporary() {
        // Cập nhật tạm thời để xem preview, không lưu vào database
        currentMovie.setTitle(edtTitle.getText().toString().trim());
        currentMovie.setYear(edtYear.getText().toString().trim());
        currentMovie.setCountry(edtCountry.getText().toString().trim());
        currentMovie.setImdbRating(edtImdbRating.getText().toString().trim());
        currentMovie.setGenres(edtGenres.getText().toString().trim());

        if (currentMovieDetail != null) {
            currentMovieDetail.setRuntime(edtRuntime.getText().toString().trim());
            currentMovieDetail.setDirector(edtDirector.getText().toString().trim());
            currentMovieDetail.setWriter(edtWriter.getText().toString().trim());
            currentMovieDetail.setActors(edtActors.getText().toString().trim());
            currentMovieDetail.setPlot(edtPlot.getText().toString().trim());
            currentMovieDetail.setAwards(edtAwards.getText().toString().trim());
            currentMovieDetail.setMetascore(edtMetascore.getText().toString().trim());
            currentMovieDetail.setImdbVotes(edtImdbVotes.getText().toString().trim());
            currentMovieDetail.setVideoUrl(edtVideoUrl.getText().toString().trim());
        }
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
        btnUpdateMovie.setEnabled(!show);
        btnCancel.setEnabled(!show);
        btnViewDetails.setEnabled(!show);
        btnDeleteMovie.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}