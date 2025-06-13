package com.example.movies_app.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movies_app.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ManageMoviesActivity extends AppCompatActivity {
    private RecyclerView moviesRecyclerView;
    private FloatingActionButton fabAddMovie;
    private ImageView backButton;
    private EditText searchEditText;
    private Button searchButton;

    // Adapter và danh sách phim (sẽ cần tạo thêm)
    // private MovieManagementAdapter movieAdapter;
    // private List<Movie> moviesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_movies);

        initViews();
        setupClickListeners();
        loadMovies();
    }

    private void initViews() {
        moviesRecyclerView = findViewById(R.id.moviesRecyclerView);
        fabAddMovie = findViewById(R.id.fabAddMovie);
        backButton = findViewById(R.id.backButton);
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);

        // Setup RecyclerView
        moviesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        fabAddMovie.setOnClickListener(v -> showAddMovieDialog());

        searchButton.setOnClickListener(v -> searchMovies());
    }

    private void loadMovies() {
        // TODO: Load movies from API or database
        Toast.makeText(this, "Đang tải danh sách phim...", Toast.LENGTH_SHORT).show();
    }

    private void searchMovies() {
        String query = searchEditText.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement search functionality
        Toast.makeText(this, "Tìm kiếm: " + query, Toast.LENGTH_SHORT).show();
    }

    private void showAddMovieDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_movie, null);

        EditText titleEdit = dialogView.findViewById(R.id.movieTitleEdit);
        EditText directorEdit = dialogView.findViewById(R.id.movieDirectorEdit);
        EditText yearEdit = dialogView.findViewById(R.id.movieYearEdit);
        EditText genreEdit = dialogView.findViewById(R.id.movieGenreEdit);
        EditText posterUrlEdit = dialogView.findViewById(R.id.moviePosterUrlEdit);

        builder.setView(dialogView)
                .setTitle("Thêm Phim Mới")
                .setPositiveButton("Thêm", (dialog, id) -> {
                    String title = titleEdit.getText().toString().trim();
                    String director = directorEdit.getText().toString().trim();
                    String year = yearEdit.getText().toString().trim();
                    String genre = genreEdit.getText().toString().trim();
                    String posterUrl = posterUrlEdit.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập tên phim", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    addMovie(title, director, year, genre, posterUrl);
                })
                .setNegativeButton("Hủy", null);

        builder.create().show();
    }

    private void addMovie(String title, String director, String year, String genre, String posterUrl) {
        // TODO: Add movie to database/API
        Toast.makeText(this, "Đã thêm phim: " + title, Toast.LENGTH_SHORT).show();
        loadMovies(); // Refresh list
    }

    public void editMovie(int movieId) {
        // TODO: Implement edit movie functionality
        Toast.makeText(this, "Chỉnh sửa phim ID: " + movieId, Toast.LENGTH_SHORT).show();
    }

    public void deleteMovie(int movieId) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa phim này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // TODO: Delete movie from database/API
                    Toast.makeText(this, "Đã xóa phim ID: " + movieId, Toast.LENGTH_SHORT).show();
                    loadMovies(); // Refresh list
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}