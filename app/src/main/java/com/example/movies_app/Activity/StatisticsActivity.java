package com.example.movies_app.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.movies_app.Database.AppDatabase;
import com.example.movies_app.R;

import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {
    private ImageView backButton;
    private TextView totalUsersText, totalMoviesText, activeUsersText, totalViewsText;
    private TextView mostViewedMovieText, mostActiveUserText, registrationTrendText;
    private CardView usersCard, moviesCard, viewsCard, trendsCard;

    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        database = AppDatabase.getInstance(this);

        initViews();
        setupClickListeners();
        loadStatistics();
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);

        // Statistics cards
        usersCard = findViewById(R.id.usersCard);
        moviesCard = findViewById(R.id.moviesCard);
        viewsCard = findViewById(R.id.viewsCard);
        trendsCard = findViewById(R.id.trendsCard);

        // Statistics text views
        totalUsersText = findViewById(R.id.totalUsersText);
        totalMoviesText = findViewById(R.id.totalMoviesText);
        activeUsersText = findViewById(R.id.activeUsersText);
        totalViewsText = findViewById(R.id.totalViewsText);
        mostViewedMovieText = findViewById(R.id.mostViewedMovieText);
        mostActiveUserText = findViewById(R.id.mostActiveUserText);
        registrationTrendText = findViewById(R.id.registrationTrendText);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        usersCard.setOnClickListener(v -> showUserStatisticsDetail());
        moviesCard.setOnClickListener(v -> showMovieStatisticsDetail());
        viewsCard.setOnClickListener(v -> showViewStatisticsDetail());
        trendsCard.setOnClickListener(v -> showTrendStatisticsDetail());
    }

    private void loadStatistics() {
        new Thread(() -> {
            try {
                // Load basic statistics - ĐÃ CÓ
                int totalUsers = database.userDao().getTotalUsersCount();
                int activeUsers = database.userDao().getActiveUsersCount();

                // Load movie statistics - MỚI THÊM (ĐÃ SỬA)
                int totalMovies = database.movieDao().getTotalMoviesCount();
                int totalViews = database.movieDao().getTotalViewsCount();

                // Load detailed statistics - MỚI THÊM
                String mostViewedMovie = database.movieDao().getMostViewedMovieWithCount();
                String mostActiveUser = database.userDao().getMostActiveUserWithCount();

                // Calculate trends - MỚI THÊM (ĐÃ SỬA)
                int newUsersThisMonth = database.userDao().getNewUsersThisMonth();
                int newUsersPreviousMonth = database.userDao().getNewUsersPreviousMonth();
                String trend = calculateTrend(newUsersThisMonth, newUsersPreviousMonth);

                // Load average rating
                double avgRating = database.movieDao().getAverageMovieRating();

                runOnUiThread(() -> {
                    // Cập nhật dữ liệu thực thay vì placeholder
                    totalUsersText.setText(String.valueOf(totalUsers));
                    activeUsersText.setText(String.valueOf(activeUsers));
                    totalMoviesText.setText(String.valueOf(totalMovies)); // Thay "150"
                    totalViewsText.setText(formatNumber(totalViews)); // Thay "12,345"

                    // Cập nhật dữ liệu chi tiết
                    mostViewedMovieText.setText(mostViewedMovie != null && !mostViewedMovie.isEmpty() ?
                            mostViewedMovie : "Chưa có dữ liệu");
                    mostActiveUserText.setText(mostActiveUser != null && !mostActiveUser.isEmpty() ?
                            mostActiveUser : "Chưa có dữ liệu");
                    registrationTrendText.setText(trend);

                    updateCardColors();
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi tải thống kê: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private String calculateTrend(int thisMonth, int previousMonth) {
        if (previousMonth == 0) {
            return thisMonth > 0 ? "+100% (tháng đầu tiên)" : "Chưa có dữ liệu";
        }

        double percentage = ((double)(thisMonth - previousMonth) / previousMonth) * 100;
        String sign = percentage > 0 ? "+" : "";
        return String.format(Locale.getDefault(), "%s%.1f%% so với tháng trước", sign, percentage);
    }

    private String formatNumber(int number) {
        if (number >= 1000000) {
            return String.format(Locale.getDefault(), "%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format(Locale.getDefault(), "%.1fK", number / 1000.0);
        } else {
            return String.valueOf(number);
        }
    }
    private void updateCardColors() {
        // Update card background colors based on data trends
        usersCard.setCardBackgroundColor(Color.parseColor("#4CAF50")); // Green for positive
        moviesCard.setCardBackgroundColor(Color.parseColor("#2196F3")); // Blue
        viewsCard.setCardBackgroundColor(Color.parseColor("#FF9800")); // Orange
        trendsCard.setCardBackgroundColor(Color.parseColor("#9C27B0")); // Purple
    }

    private void showUserStatisticsDetail() {
        // TODO: Show detailed user statistics
        Toast.makeText(this, "Chi tiết thống kê người dùng", Toast.LENGTH_SHORT).show();
    }

    private void showMovieStatisticsDetail() {
        // TODO: Show detailed movie statistics
        Toast.makeText(this, "Chi tiết thống kê phim", Toast.LENGTH_SHORT).show();
    }

    private void showViewStatisticsDetail() {
        // TODO: Show detailed view statistics
        Toast.makeText(this, "Chi tiết thống kê lượt xem", Toast.LENGTH_SHORT).show();
    }

    private void showTrendStatisticsDetail() {
        // TODO: Show detailed trend analysis
        Toast.makeText(this, "Chi tiết phân tích xu hướng", Toast.LENGTH_SHORT).show();
    }

    private void generateReport() {
        new Thread(() -> {
            try {
                // TODO: Generate and export statistics report
                runOnUiThread(() ->
                        Toast.makeText(this, "Đang tạo báo cáo thống kê...", Toast.LENGTH_SHORT).show()
                );

                // Simulate report generation
                Thread.sleep(2000);

                runOnUiThread(() ->
                        Toast.makeText(this, "Báo cáo đã được lưu!", Toast.LENGTH_SHORT).show()
                );

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi tạo báo cáo: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}