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
import com.example.movies_app.Domain.TMDbVideo;
import com.example.movies_app.Domain.TMDbVideoResponse;
import com.example.movies_app.Helper.TMDbApiService;
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
    private TMDbApiService tmdbApiService;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Khởi tạo các services và database
        tmdbApiService = new TMDbApiService(this);
        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        // Kiểm tra dữ liệu từ Intent
        checkIntentData();

        initView();
        setupTabLayout();

        // Load dữ liệu dựa trên source
        if (useLocalData && localMovie != null) {
            loadLocalMovieData();
        } else if (useTmdbData && tmdbMovie != null) {
            loadTmdbData();
        } else {
            loadMovieFromDatabase();
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
    }

    private void loadLocalMovieData() {
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
        movieTimeTxt.setText("N/A");
        movieDateTxt.setText(localMovie.getYear() != null ? localMovie.getYear() : "Unknown");

        // Set tab content
        summeryContent.setText("Thể loại: " +
                (localMovie.getGenres() != null ? localMovie.getGenres() : "Chưa xác định") +
                "\n\nQuốc gia: " +
                (localMovie.getCountry() != null ? localMovie.getCountry() : "Chưa xác định") +
                "\n\nIMDb Rating: " +
                (localMovie.getImdbRating() != null ? localMovie.getImdbRating() : "Chưa có"));

        actorsContent.setText("Thông tin diễn viên chưa có sẵn cho phim này.");

        // Setup images
        setupImagesList(localMovie.getPoster(), localMovie.getImages());

        // Cập nhật view count
        updateViewCount();
    }

    private void loadTmdbData() {
        progressBar.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);

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

        // Thêm sự kiện click cho nút xem phim
        AppCompatButton btnWatchMovie = findViewById(R.id.btnWatchMovie);
        btnWatchMovie.setOnClickListener(v -> handleWatchMovie());
    }

    private void handleWatchMovie() {
        // Hiển thị loading
        progressBar.setVisibility(View.VISIBLE);

        int movieId = getCurrentMovieId();
        String movieTitle = getCurrentMovieTitle();

        if (movieId <= 0) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Lỗi: Không thể xác định ID phim", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy video thực từ TMDb
        loadAndPlayVideo(movieId, movieTitle);
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

    private void loadAndPlayVideo(int movieId, String movieTitle) {
        // THÊM LOG ĐỂ DEBUG
        Log.d("DetailActivity", "🎬 Loading trailer for movieId: " + movieId + ", title: " + movieTitle);

        tmdbApiService.getMovieVideos(movieId, new TMDbApiService.VideoCallback() {
            @Override
            public void onSuccess(TMDbVideoResponse response) {
                progressBar.setVisibility(View.GONE);

                Log.d("DetailActivity", "✅ Video API response received!");

                if (response != null && response.hasVideos()) {
                    Log.d("DetailActivity", "📹 Found " + response.getResults().size() + " videos");

                    // In ra tất cả video để debug
                    for (TMDbVideo video : response.getResults()) {
                        Log.d("DetailActivity", "Video: " + video.getName() +
                                " | Type: " + video.getType() +
                                " | Site: " + video.getSite() +
                                " | Key: " + video.getKey());
                    }

                    // Tìm trailer YouTube
                    TMDbVideo trailer = response.getFirstYouTubeTrailer();
                    if (trailer == null) {
                        trailer = response.getFirstYouTubeVideo();
                    }

                    if (trailer != null) {
                        Log.d("DetailActivity", "🎯 Playing trailer: " + trailer.getName());
                        Log.d("DetailActivity", "🔗 YouTube URL: " + trailer.getYouTubeUrl());

                        // Phát trailer thực
                        Intent intent = new Intent(DetailActivity.this, PlayerActivity.class);
                        intent.putExtra("id", movieId);
                        intent.putExtra("title", movieTitle + " - Trailer");
                        intent.putExtra("videoUrl", trailer.getYouTubeUrl());
                        intent.putExtra("youtubeKey", trailer.getKey());
                        startActivity(intent);

                        Toast.makeText(DetailActivity.this,
                                "Đang phát trailer: " + trailer.getName(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Nếu không có video
                Log.w("DetailActivity", "❌ No trailer found for movie ID: " + movieId);
                Toast.makeText(DetailActivity.this,
                        "Không tìm thấy trailer cho phim này", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Log.e("DetailActivity", "❌ Error loading trailer: " + error);
                Toast.makeText(DetailActivity.this,
                        "Lỗi tải trailer: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void playFallbackVideo(int movieId, String movieTitle) {
        Intent intent = new Intent(DetailActivity.this, PlayerActivity.class);
        intent.putExtra("id", movieId);
        intent.putExtra("title", movieTitle);
        intent.putExtra("videoUrl", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
        startActivity(intent);

        Toast.makeText(this, "Đang phát video mẫu", Toast.LENGTH_SHORT).show();
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