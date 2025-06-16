package com.example.movies_app.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movies_app.Adapter.Top10MoviesAdapter;
import com.example.movies_app.Database.AppDatabase;
import com.example.movies_app.Database.entity.Movie;
import com.example.movies_app.R;
import com.example.movies_app.service.ReportExportService;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity implements Top10MoviesAdapter.OnMovieClickListener {

    // UI Components
    private ImageView backButton;
    private TextView totalUsersText, totalMoviesText, activeUsersText, totalViewsText;
    private CardView usersCard, moviesCard, viewsCard, trendsCard;
    private Button generateReportButton, exportDataButton;
    private Button btnDay, btnWeek, btnMonth;
    private RecyclerView top10MoviesRecyclerView;
    private LineChart statisticsChart;

    // Data & Services
    private AppDatabase database;
    private ReportExportService reportExportService;
    private Top10MoviesAdapter top10MoviesAdapter;
    private ChartPeriod currentChartPeriod = ChartPeriod.WEEK;

    // Chart Period Enum
    public enum ChartPeriod {
        DAY, WEEK, MONTH
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        database = AppDatabase.getInstance(this);
        reportExportService = new ReportExportService(this);

        initViews();
        setupClickListeners();
        setupTop10Movies();
        initChart();
        loadStatistics();
        loadTop10Movies();
        loadChartData(currentChartPeriod);
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

        // Action buttons
        generateReportButton = findViewById(R.id.generateReportButton);
        exportDataButton = findViewById(R.id.exportDataButton);

        // Chart components
        btnDay = findViewById(R.id.btnDay);
        btnWeek = findViewById(R.id.btnWeek);
        btnMonth = findViewById(R.id.btnMonth);
        statisticsChart = findViewById(R.id.statisticsChart);

        // Top 10 movies
        top10MoviesRecyclerView = findViewById(R.id.top10MoviesRecyclerView);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        // Card click listeners for detailed info
        usersCard.setOnClickListener(v -> showUserStatisticsDetail());
        moviesCard.setOnClickListener(v -> showMovieStatisticsDetail());
        viewsCard.setOnClickListener(v -> showViewStatisticsDetail());
        trendsCard.setOnClickListener(v -> showTrendStatisticsDetail());

        // Action buttons
        generateReportButton.setOnClickListener(v -> showReportOptions());
        exportDataButton.setOnClickListener(v -> showExportOptions());

        // Chart period selectors
        btnDay.setOnClickListener(v -> selectChartPeriod(ChartPeriod.DAY));
        btnWeek.setOnClickListener(v -> selectChartPeriod(ChartPeriod.WEEK));
        btnMonth.setOnClickListener(v -> selectChartPeriod(ChartPeriod.MONTH));
    }

    private void setupTop10Movies() {
        top10MoviesAdapter = new Top10MoviesAdapter(this, new ArrayList<>());
        top10MoviesAdapter.setOnMovieClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        top10MoviesRecyclerView.setLayoutManager(layoutManager);
        top10MoviesRecyclerView.setAdapter(top10MoviesAdapter);
    }

    private void initChart() {
        // Configure chart appearance
        statisticsChart.setTouchEnabled(true);
        statisticsChart.setDragEnabled(true);
        statisticsChart.setScaleEnabled(true);
        statisticsChart.setPinchZoom(true);
        statisticsChart.setDrawGridBackground(false);
        statisticsChart.setBackgroundColor(Color.WHITE);

        // Description
        Description description = new Description();
        description.setText("Thống kê lượt xem");
        description.setTextColor(Color.GRAY);
        description.setTextSize(12f);
        statisticsChart.setDescription(description);

        // X-axis configuration
        XAxis xAxis = statisticsChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        // Y-axis configuration
        YAxis leftYAxis = statisticsChart.getAxisLeft();
        leftYAxis.setTextColor(Color.BLACK);
        leftYAxis.setDrawGridLines(true);
        leftYAxis.setGridColor(Color.LTGRAY);

        YAxis rightYAxis = statisticsChart.getAxisRight();
        rightYAxis.setEnabled(false);

        // Legend
        statisticsChart.getLegend().setEnabled(true);
        statisticsChart.getLegend().setTextColor(Color.BLACK);
    }

    private void loadStatistics() {
        new Thread(() -> {
            try {
                // Load basic statistics
                int totalUsers = database.userDao().getTotalUsersCount();
                int activeUsers = database.userDao().getActiveUsersCount();
                int totalMovies = database.movieDao().getTotalMoviesCount();
                int totalViews = getTotalViewsFromViewCount();

                runOnUiThread(() -> {
                    totalUsersText.setText(formatNumber(totalUsers));
                    totalMoviesText.setText(formatNumber(totalMovies));
                    activeUsersText.setText(formatNumber(activeUsers));
                    totalViewsText.setText(formatNumber(totalViews));
                    updateCardColors();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi tải thống kê: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void loadTop10Movies() {
        new Thread(() -> {
            try {
                List<Movie> top10Movies = database.movieDao().getTopViewedMovies(10);
                runOnUiThread(() -> {
                    top10MoviesAdapter.updateData(top10Movies);
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi tải top 10 phim: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void selectChartPeriod(ChartPeriod period) {
        currentChartPeriod = period;

        // Update button states
        updateChartButtons();

        // Load new chart data
        loadChartData(period);
    }

    private void updateChartButtons() {
        // Reset all buttons
        btnDay.setBackgroundResource(R.drawable.btn_background);
        btnWeek.setBackgroundResource(R.drawable.btn_background);
        btnMonth.setBackgroundResource(R.drawable.btn_background);

        // Highlight selected button
        switch (currentChartPeriod) {
            case DAY:
                btnDay.setBackgroundColor(getResources().getColor(R.color.green));
                break;
            case WEEK:
                btnWeek.setBackgroundColor(getResources().getColor(R.color.green));
                break;
            case MONTH:
                btnMonth.setBackgroundColor(getResources().getColor(R.color.green));
                break;
        }
    }

    private void loadChartData(ChartPeriod period) {
        new Thread(() -> {
            try {
                List<Entry> entries = new ArrayList<>();
                List<String> labels = new ArrayList<>();

                // SỬ DỤNG DỮ LIỆU THỰC TỪ DATABASE
                generateRealChartData(entries, labels, period);

                runOnUiThread(() -> updateChart(entries, labels, period));
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi tải dữ liệu biểu đồ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void generateRealChartData(List<Entry> entries, List<String> labels, ChartPeriod period) {
        try {
            switch (period) {
                case DAY:
                    generateDailyViewData(entries, labels);
                    break;
                case WEEK:
                    generateWeeklyViewData(entries, labels);
                    break;
                case MONTH:
                    generateMonthlyViewData(entries, labels);
                    break;
            }
        } catch (Exception e) {
            entries.clear();
            labels.clear();
            runOnUiThread(() ->
                    Toast.makeText(StatisticsActivity.this,
                                    "Chưa có dữ liệu để hiển thị",
                                    Toast.LENGTH_SHORT)
                            .show()
            );
        }
    }
    private void generateDailyViewData(List<Entry> entries, List<String> labels) {
        try {
            // Lấy dữ liệu thực từ database cho 7 ngày gần nhất
            Calendar cal = Calendar.getInstance();

            for (int i = 6; i >= 0; i--) {
                cal.add(Calendar.DAY_OF_MONTH, -i);
                String date = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH));

                int viewsCount = database.movieDao().getViewsCountByDate(date);

                entries.add(new Entry(6 - i, viewsCount));
                labels.add(String.format("T%d/%d",
                        cal.get(Calendar.DAY_OF_MONTH),
                        cal.get(Calendar.MONTH) + 1));

                cal = Calendar.getInstance();
            }
        } catch (Exception e) {
            for (int i = 0; i < 7; i++) {
                entries.add(new Entry(i, 0));
                labels.add("Ngày " + (i + 1));
            }
        }
    }
    private void generateWeeklyViewData(List<Entry> entries, List<String> labels) {
        try {
            Calendar cal = Calendar.getInstance();

            for (int i = 7; i >= 0; i--) { // 8 tuần gần nhất
                cal.add(Calendar.WEEK_OF_YEAR, -i);

                // Lấy tuần này
                int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
                int year = cal.get(Calendar.YEAR);

                // Đếm số lượt xem trong tuần này
                int viewsCount = database.movieDao().getViewsCountByWeek(year, weekOfYear);

                entries.add(new Entry(7 - i, viewsCount));
                labels.add(String.format("T%d", weekOfYear));

                // Reset calendar
                cal = Calendar.getInstance();
            }
        } catch (Exception e) {
            // Fallback data
            for (int i = 0; i < 8; i++) {
                entries.add(new Entry(i, 0));
                labels.add("Tuần " + (i + 1));
            }
        }
    }
    private void generateMonthlyViewData(List<Entry> entries, List<String> labels) {
        try {
            Calendar cal = Calendar.getInstance();

            for (int i = 11; i >= 0; i--) { // 12 tháng gần nhất
                cal.add(Calendar.MONTH, -i);

                int month = cal.get(Calendar.MONTH) + 1; // Calendar.MONTH starts from 0
                int year = cal.get(Calendar.YEAR);

                // Đếm số lượt xem trong tháng này
                int viewsCount = database.movieDao().getViewsCountByMonth(year, month);

                entries.add(new Entry(11 - i, viewsCount));
                labels.add(String.format("T%d", month));

                // Reset calendar
                cal = Calendar.getInstance();
            }
        } catch (Exception e) {
            // Fallback data
            for (int i = 0; i < 12; i++) {
                entries.add(new Entry(i, 0));
                labels.add("T" + (i + 1));
            }
        }
    }

    private void updateChart(List<Entry> entries, List<String> labels, ChartPeriod period) {
        if (entries.isEmpty()) {
            statisticsChart.clear();
            statisticsChart.invalidate();
            return;
        }

        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, getChartLabel(period));
        dataSet.setColor(getResources().getColor(R.color.blue));
        dataSet.setCircleColor(getResources().getColor(R.color.blue));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setFillColor(getResources().getColor(R.color.blue));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(9f);
        dataSet.setValueTextColor(Color.BLACK);

        // Create line data
        LineData lineData = new LineData(dataSet);
        statisticsChart.setData(lineData);

        // Set X-axis labels
        XAxis xAxis = statisticsChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());

        // Refresh chart
        statisticsChart.invalidate();
    }

    private String getChartLabel(ChartPeriod period) {
        switch (period) {
            case DAY:
                return "Lượt xem theo ngày";
            case WEEK:
                return "Lượt xem theo tuần";
            case MONTH:
                return "Lượt xem theo tháng";
            default:
                return "Lượt xem";
        }
    }

    @Override
    public void onMovieClick(Movie movie) {
        // Handle top 10 movie click - có thể mở DetailActivity
        Toast.makeText(this, "Xem chi tiết phim: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
        // TODO: Bạn có thể thêm Intent để mở DetailActivity
    }

    // Giữ nguyên các phương thức cũ
    private void updateCardColors() {
        usersCard.setCardBackgroundColor(Color.parseColor("#4CAF50")); // Green
        moviesCard.setCardBackgroundColor(Color.parseColor("#2196F3")); // Blue
        viewsCard.setCardBackgroundColor(Color.parseColor("#FF9800")); // Orange
        trendsCard.setCardBackgroundColor(Color.parseColor("#9C27B0")); // Purple
    }

    // Các phương thức chi tiết thống kê (giữ nguyên từ code cũ)
    private void showUserStatisticsDetail() {
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
                                    "🎯 Phim đã xem: %d\n" +
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

    // Giữ nguyên các phương thức báo cáo và xuất dữ liệu
    private void showReportOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📋 Tạo Báo Cáo");
        builder.setMessage("Chọn loại báo cáo bạn muốn tạo:");

        builder.setPositiveButton("📊 Báo cáo HTML", (dialog, which) -> {
            generateDetailedReport();
        });

        builder.setNegativeButton("❌ Hủy", null);
        builder.show();
    }

    private void showExportOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📤 Xuất Dữ Liệu");
        builder.setMessage("Chọn định dạng xuất dữ liệu:");

        String[] options = {"📄 CSV (Excel)", "🔗 JSON"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // CSV
                    exportDataToCSV();
                    break;
                case 1: // JSON
                    exportDataToJSON();
                    break;
            }
        });

        builder.setNegativeButton("❌ Hủy", null);
        builder.show();
    }

    private void generateDetailedReport() {
        Toast.makeText(this, "🔄 Đang tạo báo cáo chi tiết...", Toast.LENGTH_SHORT).show();

        reportExportService.generateDetailedReport(new ReportExportService.ReportCallback() {
            @Override
            public void onSuccess(String message, String filePath) {
                runOnUiThread(() -> {
                    showSuccessDialog("📋 Tạo Báo Cáo Thành Công", message, filePath, "text/html");
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(StatisticsActivity.this, "❌ " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void exportDataToCSV() {
        Toast.makeText(this, "📤 Đang xuất dữ liệu CSV...", Toast.LENGTH_SHORT).show();

        reportExportService.exportDataToCSV(new ReportExportService.ExportCallback() {
            @Override
            public void onSuccess(String message, String filePath) {
                runOnUiThread(() -> {
                    showSuccessDialog("📊 Xuất CSV Thành Công", message, filePath, "text/csv");
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(StatisticsActivity.this, "❌ " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void exportDataToJSON() {
        Toast.makeText(this, "🔗 Đang xuất dữ liệu JSON...", Toast.LENGTH_SHORT).show();

        reportExportService.exportDataToJSON(new ReportExportService.ExportCallback() {
            @Override
            public void onSuccess(String message, String filePath) {
                runOnUiThread(() -> {
                    showSuccessDialog("🔗 Xuất JSON Thành Công", message, filePath, "application/json");
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(StatisticsActivity.this, "❌ " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showSuccessDialog(String title, String message, String filePath, String mimeType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message + "\n\n📁 Vị trí: " + filePath);

        builder.setPositiveButton("📂 Mở File", (dialog, which) -> {
            openFile(filePath, mimeType);
        });

        builder.setNegativeButton("📤 Chia Sẻ", (dialog, which) -> {
            shareFile(filePath, mimeType);
        });

        builder.setNeutralButton("✅ OK", null);
        builder.show();
    }

    private void openFile(String filePath, String mimeType) {
        try {
            File file = new File(filePath);
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Mở file với:"));
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareFile(String filePath, String mimeType) {
        try {
            File file = new File(filePath);
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Chia sẻ file:"));
        } catch (Exception e) {
            Toast.makeText(this, "Không thể chia sẻ file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Helper methods từ code cũ
    private int getTotalViewsFromViewCount() {
        try {
            return database.movieDao().getTotalViewsFromViewCount();
        } catch (Exception e) {
            try {
                return database.movieDao().getTotalViewsCount();
            } catch (Exception ex) {
                return 0;
            }
        }
    }

    private String getMostViewedMovieFromViewCount() {
        try {
            return database.movieDao().getMostViewedMovieFromViewCount();
        } catch (Exception e) {
            try {
                return database.movieDao().getMostViewedMovieWithCount();
            } catch (Exception ex) {
                return "Chưa có dữ liệu";
            }
        }
    }

    private int getViewedMoviesCount() {
        try {
            return database.movieDao().getViewedMoviesCount();
        } catch (Exception e) {
            try {
                return database.movieDao().getTotalMoviesCount();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reportExportService != null) {
            reportExportService.shutdown();
        }
    }
}