package com.example.movies_app.Activity;

import com.example.movies_app.R;

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
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.movies_app.Adapter.ImageListAdapter;
import com.example.movies_app.Domain.FilmItem;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

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
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        idFilm = getIntent().getIntExtra("id", 0);
        initView();
        setupTabLayout();
        sendRequest();
    }

    private void sendRequest() {
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
                Intent intent = new Intent(DetailActivity.this, PlayerActivity.class);
                intent.putExtra("id", idFilm);
                String testVideoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
                intent.putExtra("videoUrl", testVideoUrl);
                intent.putExtra("title", titleTxt.getText().toString());
                startActivity(intent);
            } catch (Exception e) {
                Log.e("DetailActivity", "Lỗi khi mở PlayerActivity: " + e.getMessage());
                e.printStackTrace();
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
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