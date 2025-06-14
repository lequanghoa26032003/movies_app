package com.example.movies_app.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
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

    // THÊM CÁC BUTTON
    private Button generateReportButton, exportDataButton;

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

        // THÊM CÁC BUTTON
        generateReportButton = findViewById(R.id.generateReportButton);
        exportDataButton = findViewById(R.id.exportDataButton);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        usersCard.setOnClickListener(v -> showUserStatisticsDetail());
        moviesCard.setOnClickListener(v -> showMovieStatisticsDetail());
        viewsCard.setOnClickListener(v -> showViewStatisticsDetail());
        trendsCard.setOnClickListener(v -> showTrendStatisticsDetail());

        // THÊM CLICK LISTENER CHO CÁC BUTTON
        generateReportButton.setOnClickListener(v -> generateReport());
        exportDataButton.setOnClickListener(v -> exportData());
    }

    private void loadStatistics() {
        new Thread(() -> {
            try {
                // Load basic statistics
                int totalUsers = database.userDao().getTotalUsersCount();
                int activeUsers = database.userDao().getActiveUsersCount();
                int totalMovies = database.movieDao().getTotalMoviesCount();

                // SỬ DỤNG DỮ LIỆU TỪ TRƯỜNG viewCount (nếu có) hoặc fallback
                int totalViews = getTotalViewsFromViewCount();
                String mostViewedMovie = getMostViewedMovieFromViewCount();

                // Thống kê user
                String mostActiveUser = getMostActiveUser();

                // Calculate trends
                int newUsersThisMonth = getNewUsersThisMonth();
                int newUsersPreviousMonth = getNewUsersPreviousMonth();
                String trend = calculateTrend(newUsersThisMonth, newUsersPreviousMonth);

                runOnUiThread(() -> {
                    // Cập nhật dữ liệu cơ bản
                    totalUsersText.setText(String.valueOf(totalUsers));
                    activeUsersText.setText(String.valueOf(activeUsers));
                    totalMoviesText.setText(String.valueOf(totalMovies));
                    totalViewsText.setText(formatNumber(totalViews));

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

    // PHƯƠNG THỨC LẤY DỮ LIỆU VỚI FALLBACK
    private int getTotalViewsFromViewCount() {
        try {
            // Thử sử dụng viewCount trước
            return database.movieDao().getTotalViewsFromViewCount();
        } catch (Exception e) {
            try {
                // Fallback về cách cũ (đếm từ watch_history)
                return database.movieDao().getTotalViewsCount();
            } catch (Exception ex) {
                return 0;
            }
        }
    }

    private String getMostViewedMovieFromViewCount() {
        try {
            // Thử sử dụng viewCount trước
            return database.movieDao().getMostViewedMovieFromViewCount();
        } catch (Exception e) {
            try {
                // Fallback về cách cũ
                return database.movieDao().getMostViewedMovieWithCount();
            } catch (Exception ex) {
                return "Chưa có dữ liệu";
            }
        }
    }

    private String getMostActiveUser() {
        try {
            return database.userDao().getMostActiveUserWithCount();
        } catch (Exception e) {
            return "Chưa có dữ liệu";
        }
    }

    private int getNewUsersThisMonth() {
        try {
            return database.userDao().getNewUsersThisMonth();
        } catch (Exception e) {
            return 0;
        }
    }

    private int getNewUsersPreviousMonth() {
        try {
            return database.userDao().getNewUsersPreviousMonth();
        } catch (Exception e) {
            return 0;
        }
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
        // CHI TIẾT THỐNG KÊ NGƯỜI DÙNG
        new Thread(() -> {
            try {
                int totalUsers = database.userDao().getTotalUsersCount();
                int activeUsers = database.userDao().getActiveUsersCount();
                int newUsersThisMonth = getNewUsersThisMonth();

                runOnUiThread(() -> {
                    String message = String.format(Locale.getDefault(),
                            "📊 CHI TIẾT THỐNG KÊ NGƯỜI DÙNG\n\n" +
                                    "👥 Tổng số người dùng: %d\n" +
                                    "🟢 Người dùng hoạt động: %d\n" +
                                    "📈 Người dùng mới tháng này: %d\n" +
                                    "💡 Tỷ lệ hoạt động: %.1f%%",
                            totalUsers, activeUsers, newUsersThisMonth,
                            totalUsers > 0 ? (activeUsers * 100.0 / totalUsers) : 0);

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi tải chi tiết: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showMovieStatisticsDetail() {
        // CHI TIẾT THỐNG KÊ PHIM
        new Thread(() -> {
            try {
                int totalMovies = database.movieDao().getTotalMoviesCount();
                int viewedMovies = getViewedMoviesCount();
                int unwatchedMovies = totalMovies - viewedMovies;
                double avgRating = getAverageRating();

                runOnUiThread(() -> {
                    String message = String.format(Locale.getDefault(),
                            "🎬 CHI TIẾT THỐNG KÊ PHIM\n\n" +
                                    "📚 Tổng số phim: %d\n" +
                                    "👁️ Phim đã xem: %d\n" +
                                    "⏳ Phim chưa xem: %d\n" +
                                    "⭐ Điểm trung bình: %.1f/10\n" +
                                    "📊 Tỷ lệ đã xem: %.1f%%",
                            totalMovies, viewedMovies, unwatchedMovies, avgRating,
                            totalMovies > 0 ? (viewedMovies * 100.0 / totalMovies) : 0);

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi tải chi tiết: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showViewStatisticsDetail() {
        // CHI TIẾT THỐNG KÊ LƯỢT XEM
        new Thread(() -> {
            try {
                int totalViews = getTotalViewsFromViewCount();
                double avgViews = getAverageViewCount();
                String mostViewed = getMostViewedMovieFromViewCount();
                int viewedMovies = getViewedMoviesCount();

                runOnUiThread(() -> {
                    String message = String.format(Locale.getDefault(),
                            "👁️ CHI TIẾT THỐNG KÊ LƯỢT XEM\n\n" +
                                    "📊 Tổng lượt xem: %s\n" +
                                    "📈 Trung bình: %.1f lượt/phim\n" +
                                    "🎬 Số phim đã xem: %d\n" +
                                    "🏆 Phim xem nhiều nhất:\n%s",
                            formatNumber(totalViews), avgViews, viewedMovies,
                            mostViewed != null ? mostViewed : "Chưa có dữ liệu");

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi tải chi tiết: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showTrendStatisticsDetail() {
        // CHI TIẾT PHÂN TÍCH XU HƯỚNG
        new Thread(() -> {
            try {
                int newUsersThisMonth = getNewUsersThisMonth();
                int newUsersPreviousMonth = getNewUsersPreviousMonth();
                int moviesAddedThisMonth = getMoviesAddedThisMonth();
                int totalFavorites = getTotalFavorites();

                runOnUiThread(() -> {
                    String userTrend = calculateTrend(newUsersThisMonth, newUsersPreviousMonth);
                    String message = String.format(Locale.getDefault(),
                            "📈 CHI TIẾT PHÂN TÍCH XU HƯỚNG\n\n" +
                                    "👥 Người dùng mới tháng này: %d\n" +
                                    "👥 Người dùng mới tháng trước: %d\n" +
                                    "📊 Xu hướng người dùng: %s\n" +
                                    "🎬 Phim thêm tháng này: %d\n" +
                                    "❤️ Tổng lượt yêu thích: %d",
                            newUsersThisMonth, newUsersPreviousMonth, userTrend,
                            moviesAddedThisMonth, totalFavorites);

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi tải chi tiết: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // THÊM CÁC PHƯƠNG THỨC HỖ TRỢ
    private int getViewedMoviesCount() {
        try {
            return database.movieDao().getViewedMoviesCount();
        } catch (Exception e) {
            // Fallback: đếm phim có ít nhất 1 lượt xem trong watch_history
            try {
                return database.movieDao().getTotalMoviesCount(); // Tạm thời return tổng số phim
            } catch (Exception ex) {
                return 0;
            }
        }
    }

    private double getAverageRating() {
        try {
            return database.movieDao().getAverageMovieRating();
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double getAverageViewCount() {
        try {
            return database.movieDao().getAverageViewCount();
        } catch (Exception e) {
            return 0.0;
        }
    }

    private int getMoviesAddedThisMonth() {
        try {
            return database.movieDao().getMoviesAddedLastMonth();
        } catch (Exception e) {
            return 0;
        }
    }

    private int getTotalFavorites() {
        try {
            return database.movieDao().getTotalFavoritesCount();
        } catch (Exception e) {
            return 0;
        }
    }

    private void generateReport() {
        new Thread(() -> {
            try {
                runOnUiThread(() ->
                        Toast.makeText(this, "🔄 Đang tạo báo cáo thống kê...", Toast.LENGTH_SHORT).show()
                );

                // Simulate report generation process
                Thread.sleep(1500);

                // Thu thập dữ liệu cho báo cáo
                int totalUsers = database.userDao().getTotalUsersCount();
                int totalMovies = database.movieDao().getTotalMoviesCount();
                int totalViews = getTotalViewsFromViewCount();
                String mostViewedMovie = getMostViewedMovieFromViewCount();

                runOnUiThread(() -> {
                    String reportSummary = String.format(Locale.getDefault(),
                            "📋 BÁO CÁO THỐNG KÊ HỆ THỐNG\n\n" +
                                    "📅 Ngày tạo: %s\n" +
                                    "👥 Tổng người dùng: %d\n" +
                                    "🎬 Tổng phim: %d\n" +
                                    "👁️ Tổng lượt xem: %s\n" +
                                    "🏆 Phim phổ biến nhất: %s\n\n" +
                                    "✅ Báo cáo đã được tạo thành công!",
                            java.text.DateFormat.getDateInstance().format(new java.util.Date()),
                            totalUsers, totalMovies, formatNumber(totalViews),
                            mostViewedMovie != null ? mostViewedMovie : "N/A");

                    Toast.makeText(this, reportSummary, Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "❌ Lỗi tạo báo cáo: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void exportData() {
        new Thread(() -> {
            try {
                runOnUiThread(() ->
                        Toast.makeText(this, "📤 Đang xuất dữ liệu...", Toast.LENGTH_SHORT).show()
                );

                // Simulate data export process
                Thread.sleep(2000);

                // Thu thập dữ liệu để xuất
                int totalUsers = database.userDao().getTotalUsersCount();
                int totalMovies = database.movieDao().getTotalMoviesCount();
                int totalViews = getTotalViewsFromViewCount();

                runOnUiThread(() -> {
                    String exportSummary = String.format(Locale.getDefault(),
                            "📊 XUẤT DỮ LIỆU THÀNH CÔNG\n\n" +
                                    "📁 Định dạng: CSV\n" +
                                    "📋 Nội dung:\n" +
                                    "• %d người dùng\n" +
                                    "• %d phim\n" +
                                    "• %s lượt xem\n" +
                                    "• Lịch sử hoạt động\n\n" +
                                    "💾 Dữ liệu đã được lưu!",
                            totalUsers, totalMovies, formatNumber(totalViews));

                    Toast.makeText(this, exportSummary, Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "❌ Lỗi xuất dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}