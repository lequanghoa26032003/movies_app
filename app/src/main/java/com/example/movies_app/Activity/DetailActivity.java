package com.example.movies_app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.movies_app.Adapter.ImageListAdapter;
import com.example.movies_app.Database.AppDatabase;
import com.example.movies_app.Database.entity.Movie;
import com.example.movies_app.Database.entity.MovieDetail;
import com.example.movies_app.Domain.FilmItem;
import com.example.movies_app.Domain.TMDbMovie;
import com.example.movies_app.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailActivity extends AppCompatActivity {
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;
    private ProgressBar progressBar;
    private TextView titleTxt, movieRateTxt, movieTimeTxt, movieDateTxt, movieSummaryInfo, movieActorsInfo;
    private NestedScrollView scrollView;
    private int idFilm;
    private ShapeableImageView pic1;
    private ImageView pic2, backImg;
    private RecyclerView.Adapter adapterImgList;
    private RecyclerView recyclerView;

    // Thêm biến cho TabLayout và các container
    private TabLayout tabLayout;
    private LinearLayout summeryContainer, actorsContainer;
    private TextView summeryContent, actorsContent;

    // Thêm biến cho TMDb data và Local movie
    private TMDbMovie tmdbMovie;
    private Movie localMovie;
    private boolean useTmdbData = false;
    private boolean useLocalData = false;
    private AppDatabase database;
    private ExecutorService executorService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Kiểm tra dữ liệu từ Intent
        checkIntentData();
        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        initView();
        setupTabLayout();

        if (useLocalData && localMovie != null) {
            loadLocalMovieData();
        } else if (useTmdbData && tmdbMovie != null) {
            loadTmdbData();
        } else {
            // THAY ĐỔI: Gọi method mới thay vì sendRequest()
            loadMovieFromDatabase();
        }
    }
    private void loadMovieFromDatabase() {
        if (idFilm <= 0) {
            Log.e("DetailActivity", "Invalid movie ID: " + idFilm);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Lỗi: ID phim không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);

        executorService.execute(() -> {
            try {
                // Lấy thông tin phim từ database
                Movie movie = database.movieDao().getMovieById(idFilm);
                MovieDetail movieDetail = database.movieDao().getMovieDetailById(idFilm);

                runOnUiThread(() -> {
                    if (movie != null) {
                        displayMovieFromDatabase(movie, movieDetail);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(DetailActivity.this, "Không tìm thấy thông tin phim", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

            } catch (Exception e) {
                Log.e("DetailActivity", "Error loading movie: " + e.getMessage());
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(DetailActivity.this, "Lỗi khi tải thông tin phim", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }
    private void displayMovieFromDatabase(Movie movie, MovieDetail movieDetail) {
        progressBar.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);

        // Load hình ảnh
        Glide.with(DetailActivity.this)
                .load(movie.getPoster())
                .placeholder(R.drawable.placeholder_movie)
                .error(R.drawable.placeholder_movie)
                .into(pic1);

        Glide.with(DetailActivity.this)
                .load(movie.getPoster())
                .placeholder(R.drawable.placeholder_movie)
                .error(R.drawable.placeholder_movie)
                .into(pic2);

        // Set thông tin cơ bản
        titleTxt.setText(movie.getTitle() != null ? movie.getTitle() : "Unknown Title");
        movieRateTxt.setText(movie.getImdbRating() != null ? movie.getImdbRating() : "N/A");
        movieDateTxt.setText(movie.getYear() != null ? movie.getYear() : "Unknown");

        // Set runtime từ MovieDetail
        if (movieDetail != null && movieDetail.getRuntime() != null) {
            movieTimeTxt.setText(movieDetail.getRuntime());
        } else {
            movieTimeTxt.setText("N/A");
        }

        // Cập nhật nội dung cho các tab
        if (movieDetail != null && movieDetail.getPlot() != null) {
            summeryContent.setText(movieDetail.getPlot());
        } else {
            summeryContent.setText("Nội dung: " +
                    (movie.getGenres() != null ? movie.getGenres() : "Chưa có") +
                    "\nQuốc gia: " +
                    (movie.getCountry() != null ? movie.getCountry() : "Chưa có"));
        }

        if (movieDetail != null && movieDetail.getActors() != null) {
            actorsContent.setText(movieDetail.getActors());
        } else {
            actorsContent.setText("Thông tin diễn viên chưa có sẵn.");
        }

        // Cập nhật RecyclerView cho tab "Liên quan"
        List<String> images = new ArrayList<>();
        if (movie.getPoster() != null) images.add(movie.getPoster());
        if (movie.getImages() != null && !movie.getImages().equals(movie.getPoster())) {
            images.add(movie.getImages());
        }

        if (!images.isEmpty()) {
            adapterImgList = new ImageListAdapter(images);
            recyclerView.setAdapter(adapterImgList);
        }
    }

    private void checkIntentData() {
        Intent intent = getIntent();

        // Kiểm tra xem có dữ liệu Local Movie không
        if (intent.hasExtra("object")) {
            localMovie = (Movie) intent.getSerializableExtra("object");
            useLocalData = true;
            Log.d("DetailActivity", "Using local movie data: " + localMovie.getTitle());
        }
        // Kiểm tra xem có dữ liệu TMDb không
        else if (intent.hasExtra("tmdb_movie")) {
            tmdbMovie = (TMDbMovie) intent.getSerializableExtra("tmdb_movie");
            useTmdbData = intent.getBooleanExtra("use_tmdb_data", false);
            Log.d("DetailActivity", "Using TMDb movie data: " + tmdbMovie.getTitle());
        }

        // Lấy ID phim (cho trường hợp fallback)
        idFilm = intent.getIntExtra("id", 0);
        Log.d("DetailActivity", "Movie ID: " + idFilm);
    }

    private void loadLocalMovieData() {
        // Hiển thị dữ liệu từ Local Movie trực tiếp
        progressBar.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);

        // Load images
        Glide.with(DetailActivity.this)
                .load(localMovie.getPoster())
                .placeholder(R.drawable.placeholder_movie)
                .error(R.drawable.placeholder_movie)
                .into(pic1);

        Glide.with(DetailActivity.this)
                .load(localMovie.getPoster())
                .placeholder(R.drawable.placeholder_movie)
                .error(R.drawable.placeholder_movie)
                .into(pic2);

        // Set basic info
        titleTxt.setText(localMovie.getTitle() != null ? localMovie.getTitle() : "Unknown Title");
        movieRateTxt.setText("N/A"); // Local movie might not have rating
        movieTimeTxt.setText("N/A"); // Local movie might not have runtime
        movieDateTxt.setText(localMovie.getYear() != null ? localMovie.getYear() : "Unknown");

        // Set tab content
        summeryContent.setText("Thông tin chi tiết chưa có sẵn cho phim này.\n\nThể loại: " +
                (localMovie.getGenres() != null ? localMovie.getGenres() : "Chưa xác định") +
                "\n\nQuốc gia: " +
                (localMovie.getCountry() != null ? localMovie.getCountry() : "Chưa xác định") +
                "\n\nIMDb Rating: " +
                (localMovie.getImdbRating() != null ? localMovie.getImdbRating() : "Chưa có"));

        actorsContent.setText("Thông tin diễn viên chưa có sẵn cho phim này.");

        // Set up images for related tab
        List<String> images = new ArrayList<>();
        if (localMovie.getPoster() != null && !localMovie.getPoster().isEmpty()) {
            images.add(localMovie.getPoster());
        }

        if (!images.isEmpty()) {
            adapterImgList = new ImageListAdapter(images);
            recyclerView.setAdapter(adapterImgList);
        }
    }

    private void loadTmdbData() {
        // Hiển thị dữ liệu từ TMDb trực tiếp
        progressBar.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);

        // Load images
        Glide.with(DetailActivity.this)
                .load(tmdbMovie.getFullPosterUrl())
                .into(pic1);

        Glide.with(DetailActivity.this)
                .load(tmdbMovie.getFullBackdropUrl())
                .into(pic2);

        // Set basic info
        titleTxt.setText(tmdbMovie.getTitle());
        movieRateTxt.setText(tmdbMovie.getRated() != null ? tmdbMovie.getRated() : "N/A");
        movieTimeTxt.setText(tmdbMovie.getRuntime() != null ? tmdbMovie.getRuntime() : "N/A");
        movieDateTxt.setText(tmdbMovie.getReleaseDate());

        // Set tab content
        summeryContent.setText(tmdbMovie.getOverview());
        actorsContent.setText(tmdbMovie.getActors() != null ? tmdbMovie.getActors() : "Thông tin diễn viên chưa có");

        // Set up images for related tab
        List<String> images = new ArrayList<>();
        if (tmdbMovie.getFullPosterUrl() != null && !tmdbMovie.getFullPosterUrl().isEmpty()) {
            images.add(tmdbMovie.getFullPosterUrl());
        }
        if (tmdbMovie.getFullBackdropUrl() != null && !tmdbMovie.getFullBackdropUrl().isEmpty()) {
            images.add(tmdbMovie.getFullBackdropUrl());
        }

        if (!images.isEmpty()) {
            adapterImgList = new ImageListAdapter(images);
            recyclerView.setAdapter(adapterImgList);
        }
    }

    private void sendRequest() {
        // Kiểm tra ID hợp lệ trước khi gọi API
        if (idFilm <= 0) {
            Log.e("DetailActivity", "Invalid movie ID: " + idFilm);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Lỗi: ID phim không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mRequestQueue = Volley.newRequestQueue(this);
        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);

        mStringRequest = new StringRequest(Request.Method.GET, "https://moviesapi.ir/api/v1/movies/"+idFilm, response -> {
            Gson gson = new Gson();
            progressBar.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);

            FilmItem item = gson.fromJson(response, FilmItem.class);

            Glide.with(DetailActivity.this)
                    .load(item.getPoster())
                    .into(pic1);

            Glide.with(DetailActivity.this)
                    .load(item.getPoster())
                    .into(pic2);

            titleTxt.setText(item.getTitle());
            movieRateTxt.setText(item.getRated());
            movieTimeTxt.setText(item.getRuntime());
            movieDateTxt.setText(item.getReleased());

            // Cập nhật nội dung cho các tab
            summeryContent.setText(item.getPlot());
            actorsContent.setText(item.getActors());

            // Cập nhật RecyclerView cho tab "Liên quan"
            if(item.getImages() != null) {
                adapterImgList = new ImageListAdapter(item.getImages());
                recyclerView.setAdapter(adapterImgList);
            }

        }, error -> {
            progressBar.setVisibility(View.GONE);
            Log.i("uilover", "onErrorResponse: "+error.toString());
            Toast.makeText(this, "Lỗi khi tải dữ liệu phim", Toast.LENGTH_SHORT).show();
            finish();
        });
        mRequestQueue.add(mStringRequest);
    }

    private void initView() {
        // Các thành phần UI chính
        titleTxt = findViewById(R.id.movieNameTxt);
        progressBar = findViewById(R.id.detailLoading);
        scrollView = findViewById(R.id.scrollView2);
        pic1 = findViewById(R.id.posterNormalImg);
        pic2 = findViewById(R.id.posterBigImg);
        movieRateTxt = findViewById(R.id.movieRateTxt);
        movieTimeTxt = findViewById(R.id.movieTimeTxt);
        movieDateTxt = findViewById(R.id.movieDateTxt);
        backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(v -> finish());

        // Khởi tạo các thành phần cho TabLayout
        tabLayout = findViewById(R.id.tabLayout);

        // Thêm sự kiện click cho nút xem phim
        AppCompatButton btnWatchMovie = findViewById(R.id.btnWatchMovie);
        btnWatchMovie.setOnClickListener(v -> {
            try {
                // Cập nhật view count
                updateViewCount();

                Intent intent = new Intent(DetailActivity.this, PlayerActivity.class);
                intent.putExtra("id", idFilm);
                intent.putExtra("title", titleTxt.getText().toString() + " - Trailer");

                // Tạo URL trailer YouTube search
                String movieTitle = titleTxt.getText().toString();
                String trailerSearchUrl = "https://www.youtube.com/results?search_query=" +
                        movieTitle.replace(" ", "+") + "+trailer";

                // Sử dụng URL trailer hoặc video mẫu
                intent.putExtra("videoUrl", getTrailerUrl(movieTitle));

                startActivity(intent);
            } catch (Exception e) {
                Log.e("DetailActivity", "Error opening player: " + e.getMessage());
                Toast.makeText(DetailActivity.this, "Lỗi khi mở video", Toast.LENGTH_SHORT).show();
            }
        });

        // Khởi tạo các container và nội dung
        summeryContainer = findViewById(R.id.summeryContainer);
        actorsContainer = findViewById(R.id.actorsContainer);
        summeryContent = findViewById(R.id.summeryContent);
        actorsContent = findViewById(R.id.actorsContent);

        // Khởi tạo RecyclerView cho tab "Liên quan"
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }
    private String getTrailerUrl(String movieTitle) {
        // Kiểm tra trong database có video URL không
        executorService.execute(() -> {
            try {
                MovieDetail detail = database.movieDao().getMovieDetailById(idFilm);
                if (detail != null && detail.getVideoUrl() != null) {
                    // Có video URL trong database
                    return;
                }
            } catch (Exception e) {
                Log.e("DetailActivity", "Error getting video URL: " + e.getMessage());
            }
        });

        // Fallback: Dùng video mẫu
        return "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
    private void updateViewCount() {
        executorService.execute(() -> {
            try {
                Movie movie = database.movieDao().getMovieById(idFilm);
                if (movie != null) {
                    movie.incrementViewCount();
                    database.movieDao().updateMovie(movie);
                }
            } catch (Exception e) {
                Log.e("DetailActivity", "Error updating view count: " + e.getMessage());
            }
        });
    }

    private void setupTabLayout() {
        // Thiết lập TabLayout listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Ẩn tất cả các container trước
                summeryContainer.setVisibility(View.GONE);
                actorsContainer.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);

                // Hiển thị container tương ứng với tab được chọn
                switch (tab.getPosition()) {
                    case 0: // Tab "Nội dung"
                        summeryContainer.setVisibility(View.VISIBLE);
                        break;
                    case 1: // Tab "Diễn viên"
                        actorsContainer.setVisibility(View.VISIBLE);
                        break;
                    case 2: // Tab "Liên quan"
                        recyclerView.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Không cần xử lý
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Không cần xử lý
            }
        });

        // Mặc định chọn tab đầu tiên khi mở activity
        TabLayout.Tab firstTab = tabLayout.getTabAt(0);
        if (firstTab != null) {
            firstTab.select();
            summeryContainer.setVisibility(View.VISIBLE);
            actorsContainer.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }
    }
}