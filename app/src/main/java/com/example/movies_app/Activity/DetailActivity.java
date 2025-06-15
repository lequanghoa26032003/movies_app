package com.example.movies_app.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.core.content.ContextCompat;
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
import com.example.movies_app.Domain.TMDbVideo;
import com.example.movies_app.Domain.TMDbVideoResponse;
import com.example.movies_app.Helper.TMDbApiService;
import com.example.movies_app.R;
import com.example.movies_app.service.FavoriteService;
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
    private MovieDetail currentMovieDetail; // THÊM BIẾN NÀY
    private boolean useTmdbData = false;
    private boolean useLocalData = false;
    private AppDatabase database;
    private TMDbApiService tmdbApiService;
    private ExecutorService executorService;
    private ImageView btnFavorite;
    private boolean isFavorite = false;
    private int currentUserId = -1;
    private FavoriteService favoriteService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Khởi tạo các services và database
        tmdbApiService = new TMDbApiService(this);
        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        favoriteService = FavoriteService.getInstance(this);

        getCurrentUser();
        // Kiểm tra dữ liệu từ Intent
        checkIntentData();

        initView();
        setupTabLayout();

        if (useLocalData && localMovie != null) {
            loadLocalMovieData();
        } else if (useTmdbData && tmdbMovie != null) {
            loadTmdbData();
        } else {
            loadMovieFromDatabase();
        }
    }
    private void getCurrentUser() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);
        Log.d("DetailActivity", "Current user ID: " + currentUserId);
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
                        // QUAN TRỌNG: Lưu movieDetail vào biến global
                        currentMovieDetail = movieDetail;
                        displayMovieFromDatabase(movie, movieDetail);
                        // Cập nhật view count
                        updateViewCount();
                    } else {
                        // Nếu không có trong database, thử gọi API
                        sendRequest();
                    }
                });

            } catch (Exception e) {
                Log.e("DetailActivity", "Error loading movie: " + e.getMessage());
                runOnUiThread(() -> {
                    // Fallback to API nếu database lỗi
                    sendRequest();
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
        setupImagesList(movie.getPoster(), movie.getImages());

        checkFavoriteStatus();
    }

    private void loadLocalMovieData() {
        progressBar.setVisibility(View.VISIBLE);

        // THÊM: Load MovieDetail cho localMovie
        executorService.execute(() -> {
            try {
                MovieDetail movieDetail = database.movieDao().getMovieDetailById(localMovie.getId());

                runOnUiThread(() -> {
                    currentMovieDetail = movieDetail; // Lưu vào biến global
                    displayLocalMovieData(movieDetail);
                });

            } catch (Exception e) {
                Log.e("DetailActivity", "Error loading local movie detail: " + e.getMessage());
                runOnUiThread(() -> {
                    currentMovieDetail = null;
                    displayLocalMovieData(null);
                });
            }
        });
    }

    private void displayLocalMovieData(MovieDetail movieDetail) {
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
        movieRateTxt.setText(localMovie.getImdbRating() != null ? localMovie.getImdbRating() : "N/A");
        movieTimeTxt.setText(movieDetail != null && movieDetail.getRuntime() != null ? movieDetail.getRuntime() : "N/A");
        movieDateTxt.setText(localMovie.getYear() != null ? localMovie.getYear() : "Unknown");

        // Set tab content
        if (movieDetail != null && movieDetail.getPlot() != null) {
            summeryContent.setText(movieDetail.getPlot());
        } else {
            summeryContent.setText("Thể loại: " +
                    (localMovie.getGenres() != null ? localMovie.getGenres() : "Chưa xác định") +
                    "\n\nQuốc gia: " +
                    (localMovie.getCountry() != null ? localMovie.getCountry() : "Chưa xác định") +
                    "\n\nIMDb Rating: " +
                    (localMovie.getImdbRating() != null ? localMovie.getImdbRating() : "Chưa có"));
        }

        if (movieDetail != null && movieDetail.getActors() != null) {
            actorsContent.setText(movieDetail.getActors());
        } else {
            actorsContent.setText("Thông tin diễn viên chưa có sẵn cho phim này.");
        }

        // Setup images
        setupImagesList(localMovie.getPoster(), localMovie.getImages());

        // Cập nhật view count
        updateViewCount();
        checkFavoriteStatus();
    }

    private void loadTmdbData() {
        progressBar.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);

        // THÊM: Đặt currentMovieDetail = null cho TMDb data
        currentMovieDetail = null;

        // Load images
        Glide.with(DetailActivity.this)
                .load(tmdbMovie.getFullPosterUrl())
                .placeholder(R.drawable.placeholder_movie)
                .error(R.drawable.placeholder_movie)
                .into(pic1);

        Glide.with(DetailActivity.this)
                .load(tmdbMovie.getFullBackdropUrl())
                .placeholder(R.drawable.placeholder_movie)
                .error(R.drawable.placeholder_movie)
                .into(pic2);

        // Set basic info
        titleTxt.setText(tmdbMovie.getTitle());
        movieRateTxt.setText(String.format("%.1f", tmdbMovie.getVoteAverage()));
        movieTimeTxt.setText(tmdbMovie.getRuntime() != null ? tmdbMovie.getRuntime() : "N/A");
        movieDateTxt.setText(tmdbMovie.getReleaseDate());

        // Set tab content
        summeryContent.setText(tmdbMovie.getOverview());
        actorsContent.setText(tmdbMovie.getActors() != null ? tmdbMovie.getActors() : "Thông tin diễn viên chưa có");

        // Setup images
        setupImagesList(tmdbMovie.getFullPosterUrl(), tmdbMovie.getFullBackdropUrl());
        checkFavoriteStatus();
    }

    private void setupImagesList(String poster, String backdrop) {
        List<String> images = new ArrayList<>();
        if (poster != null && !poster.isEmpty()) {
            images.add(poster);
        }
        if (backdrop != null && !backdrop.isEmpty() && !backdrop.equals(poster)) {
            images.add(backdrop);
        }

        if (!images.isEmpty()) {
            adapterImgList = new ImageListAdapter(images);
            recyclerView.setAdapter(adapterImgList);
        }
    }

    private void sendRequest() {
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

        mStringRequest = new StringRequest(Request.Method.GET,
                "https://moviesapi.ir/api/v1/movies/" + idFilm,
                response -> {
                    try {
                        Gson gson = new Gson();
                        FilmItem item = gson.fromJson(response, FilmItem.class);

                        progressBar.setVisibility(View.GONE);
                        scrollView.setVisibility(View.VISIBLE);

                        // Load images
                        Glide.with(DetailActivity.this)
                                .load(item.getPoster())
                                .placeholder(R.drawable.placeholder_movie)
                                .error(R.drawable.placeholder_movie)
                                .into(pic1);

                        Glide.with(DetailActivity.this)
                                .load(item.getPoster())
                                .placeholder(R.drawable.placeholder_movie)
                                .error(R.drawable.placeholder_movie)
                                .into(pic2);

                        // Set info
                        titleTxt.setText(item.getTitle());
                        movieRateTxt.setText(item.getRated());
                        movieTimeTxt.setText(item.getRuntime());
                        movieDateTxt.setText(item.getReleased());

                        // Set tab content
                        summeryContent.setText(item.getPlot());
                        actorsContent.setText(item.getActors());

                        // Setup images
                        if (item.getImages() != null) {
                            adapterImgList = new ImageListAdapter(item.getImages());
                            recyclerView.setAdapter(adapterImgList);
                        }

                    } catch (Exception e) {
                        Log.e("DetailActivity", "Error parsing response: " + e.getMessage());
                        Toast.makeText(this, "Lỗi xử lý dữ liệu phim", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("DetailActivity", "API Error: " + error.toString());
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

        // Khởi tạo các container và nội dung
        summeryContainer = findViewById(R.id.summeryContainer);
        actorsContainer = findViewById(R.id.actorsContainer);
        summeryContent = findViewById(R.id.summeryContent);
        actorsContent = findViewById(R.id.actorsContent);

        // Khởi tạo RecyclerView cho tab "Liên quan"
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        btnFavorite = findViewById(R.id.btnFavorite);
        btnFavorite.setOnClickListener(v -> toggleFavorite());

        // Thêm sự kiện click cho nút xem phim
        AppCompatButton btnWatchMovie = findViewById(R.id.btnWatchMovie);
        btnWatchMovie.setOnClickListener(v -> handleWatchMovie());
    }
    private void checkFavoriteStatus() {
        if (currentUserId == -1) {
            btnFavorite.setVisibility(View.GONE);
            return;
        }

        int movieId = getCurrentMovieId();
        if (movieId <= 0) return;

        favoriteService.checkFavoriteStatus(currentUserId, movieId, new FavoriteService.FavoriteCheckCallback() {
            @Override
            public void onResult(boolean isFavorite) {
                DetailActivity.this.isFavorite = isFavorite;
                updateFavoriteButtonUI();
            }

            @Override
            public void onError(String error) {
                Log.e("DetailActivity", "Error checking favorite status: " + error);
            }
        });
    }
    private void updateFavoriteButtonUI() {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
            btnFavorite.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_light));
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorite_border);
            btnFavorite.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
        }
    }

    // Method để toggle trạng thái yêu thích
    private void toggleFavorite() {
        if (currentUserId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            return;
        }

        int movieId = getCurrentMovieId();
        if (movieId <= 0) {
            Toast.makeText(this, "Lỗi: Không thể xác định phim", Toast.LENGTH_SHORT).show();
            return;
        }

        favoriteService.toggleFavorite(currentUserId, movieId, new FavoriteService.FavoriteCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(DetailActivity.this, message, Toast.LENGTH_SHORT).show();
                // Refresh trạng thái
                checkFavoriteStatus();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(DetailActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    // ============ PHẦN MỚI: XỬ LÝ XEM PHIM TỪ DATABASE ============
    private void handleWatchMovie() {
        Log.d("DetailActivity", "🎬 Handle watch movie clicked");

        // Hiển thị loading
        progressBar.setVisibility(View.VISIBLE);

        int movieId = getCurrentMovieId();
        String movieTitle = getCurrentMovieTitle();

        if (movieId <= 0) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Lỗi: Không thể xác định ID phim", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ SỬA ĐỔI CHÍNH: Lấy video từ database thay vì TMDb API
        loadVideoFromDatabase(movieId, movieTitle);
    }

    private void loadVideoFromDatabase(int movieId, String movieTitle) {
        Log.d("DetailActivity", "🎬 Loading video from database for movieId: " + movieId);

        // Nếu đã có currentMovieDetail, dùng luôn
        if (currentMovieDetail != null) {
            processVideoUrl(currentMovieDetail.getVideoUrl(), movieTitle);
            return;
        }

        // Nếu chưa có, query từ database
        executorService.execute(() -> {
            try {
                MovieDetail movieDetail = database.movieDao().getMovieDetailById(movieId);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);

                    if (movieDetail != null && movieDetail.getVideoUrl() != null && !movieDetail.getVideoUrl().isEmpty()) {
                        processVideoUrl(movieDetail.getVideoUrl(), movieTitle);
                    } else {
                        // Fallback: Thử TMDb API nếu không có video trong database
                        Log.w("DetailActivity", "No video URL in database, falling back to TMDb API");
                        loadAndPlayVideoFromTMDb(movieId, movieTitle);
                    }
                });

            } catch (Exception e) {
                Log.e("DetailActivity", "Error loading video from database: " + e.getMessage());
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(DetailActivity.this, "Lỗi khi tải video từ database", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void processVideoUrl(String videoUrl, String movieTitle) {
        Log.d("DetailActivity", "🎬 Processing video URL: " + videoUrl);

        if (videoUrl == null || videoUrl.isEmpty()) {
            Toast.makeText(this, "Không có URL video cho phim này", Toast.LENGTH_SHORT).show();
            return;
        }

        // Xử lý URL và phát video
        Intent intent = new Intent(DetailActivity.this, PlayerActivity.class);
        intent.putExtra("title", movieTitle);

        // Kiểm tra loại URL
        if (videoUrl.contains("youtube.com") || videoUrl.contains("youtu.be")) {
            // Xử lý YouTube URL
            String youtubeKey = extractYouTubeKey(videoUrl);
            if (youtubeKey != null) {
                intent.putExtra("youtubeKey", youtubeKey);
                intent.putExtra("videoUrl", videoUrl);
                Log.d("DetailActivity", "🎬 Playing YouTube video: " + youtubeKey);
            } else {
                intent.putExtra("videoUrl", videoUrl);
                Log.d("DetailActivity", "🎬 Playing YouTube URL directly: " + videoUrl);
            }
        } else {
            // Direct video URL (MP4, etc.)
            intent.putExtra("videoUrl", videoUrl);
            Log.d("DetailActivity", "🎬 Playing direct video URL: " + videoUrl);
        }

        startActivity(intent);
        Toast.makeText(this, "Đang phát phim: " + movieTitle, Toast.LENGTH_SHORT).show();
    }

    private String extractYouTubeKey(String url) {
        try {
            // Xử lý các format YouTube URL khác nhau
            if (url.contains("youtube.com/watch?v=")) {
                return url.split("v=")[1].split("&")[0];
            } else if (url.contains("youtu.be/")) {
                return url.split("youtu.be/")[1].split("\\?")[0];
            } else if (url.contains("youtube.com/embed/")) {
                return url.split("embed/")[1].split("\\?")[0];
            }
        } catch (Exception e) {
            Log.e("DetailActivity", "Error extracting YouTube key: " + e.getMessage());
        }
        return null;
    }

    // Fallback method (giữ nguyên logic cũ)
    private void loadAndPlayVideoFromTMDb(int movieId, String movieTitle) {
        Log.d("DetailActivity", "🎬 Fallback: Loading trailer from TMDb for movieId: " + movieId);

        tmdbApiService.getMovieVideos(movieId, new TMDbApiService.VideoCallback() {
            @Override
            public void onSuccess(TMDbVideoResponse response) {
                progressBar.setVisibility(View.GONE);

                if (response != null && response.hasVideos()) {
                    // Tìm trailer YouTube
                    TMDbVideo trailer = response.getFirstYouTubeTrailer();
                    if (trailer == null) {
                        trailer = response.getFirstYouTubeVideo();
                    }

                    if (trailer != null) {
                        // Phát trailer từ TMDb
                        Intent intent = new Intent(DetailActivity.this, PlayerActivity.class);
                        intent.putExtra("title", movieTitle + " - Trailer");
                        intent.putExtra("videoUrl", trailer.getYouTubeUrl());
                        intent.putExtra("youtubeKey", trailer.getKey());
                        startActivity(intent);

                        Toast.makeText(DetailActivity.this,
                                "Đang phát trailer: " + trailer.getName(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Không có video
                Toast.makeText(DetailActivity.this,
                        "Không tìm thấy video cho phim này", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DetailActivity.this,
                        "Lỗi tải video: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private int getCurrentMovieId() {
        int movieId = 0;

        if (useLocalData && localMovie != null) {
            movieId = localMovie.getId();
            Log.d("DetailActivity", "📱 Using LOCAL movie ID: " + movieId);
        } else if (useTmdbData && tmdbMovie != null) {
            movieId = tmdbMovie.getId();
            Log.d("DetailActivity", "🌐 Using TMDB movie ID: " + movieId);
        } else {
            movieId = idFilm;
            Log.d("DetailActivity", "🔢 Using intent movie ID: " + movieId);
        }

        return movieId;
    }

    private String getCurrentMovieTitle() {
        if (useLocalData && localMovie != null) {
            return localMovie.getTitle();
        } else if (useTmdbData && tmdbMovie != null) {
            return tmdbMovie.getTitle();
        } else {
            return titleTxt.getText().toString();
        }
    }

    private void updateViewCount() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.execute(() -> {
                try {
                    int movieId = getCurrentMovieId();
                    if (movieId > 0) {
                        Movie movie = database.movieDao().getMovieById(movieId);
                        if (movie != null) {
                            movie.incrementViewCount();
                            database.movieDao().updateMovie(movie);
                            Log.d("DetailActivity", "View count updated for movie: " + movieId);
                        }
                    }
                } catch (Exception e) {
                    Log.e("DetailActivity", "Error updating view count: " + e.getMessage());
                }
            });
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }

        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(this);
        }
    }
}