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
        // Reset all buttons to default
        btnDay.setBackgroundResource(R.drawable.btn_background);
        btnWeek.setBackgroundResource(R.drawable.btn_background);
        btnMonth.setBackgroundResource(R.drawable.btn_background);

        // Set text color to white for all
        btnDay.setTextColor(getResources().getColor(android.R.color.white));
        btnWeek.setTextColor(getResources().getColor(android.R.color.white));
        btnMonth.setTextColor(getResources().getColor(android.R.color.white));

        // Highlight selected button
        switch (currentChartPeriod) {
            case DAY:
                btnDay.setBackgroundResource(R.drawable.btn_background_green);
                break;
            case WEEK:
                btnWeek.setBackgroundResource(R.drawable.btn_background_green);
                break;
            case MONTH:
                btnMonth.setBackgroundResource(R.drawable.btn_background_green);
                break;
        }
    }

    private void loadChartData(ChartPeriod period) {
        new Thread(() -> {
            try {
                List<Entry> entries = new ArrayList<>();
                List<String> labels = new ArrayList<>();

                boolean hasData = generateRealChartData(entries, labels, period);

                runOnUiThread(() -> {
                    if (hasData && !entries.isEmpty()) {
                        updateChart(entries, labels, period);
                    } else {
                        showNoDataChart(period);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showNoDataChart(period);
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }


    private boolean generateRealChartData(List<Entry> entries, List<String> labels, ChartPeriod period) {
        try {
            switch (period) {
                case DAY:
                    return generateDailyViewData(entries, labels);
                case WEEK:
                    return generateWeeklyViewData(entries, labels);
                case MONTH:
                    return generateMonthlyViewData(entries, labels);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    private boolean generateDailyViewData(List<Entry> entries, List<String> labels) {
        try {
            boolean hasAnyData = false;

            // Lấy 7 ngày gần nhất
            for (int i = 0; i < 7; i++) {
                Calendar dayCal = Calendar.getInstance();
                dayCal.add(Calendar.DAY_OF_MONTH, -(6 - i)); // Từ 6 ngày trước đến hôm nay

                String date = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                        dayCal.get(Calendar.YEAR),
                        dayCal.get(Calendar.MONTH) + 1,
                        dayCal.get(Calendar.DAY_OF_MONTH));

                int viewsCount = database.movieDao().getViewsCountByDate(date);

                if (viewsCount > 0) {
                    hasAnyData = true;
                }

                entries.add(new Entry(i, viewsCount));

                // Tạo label đẹp: "T2 16/6" thay vì "T16/6"
                String dayOfWeek = getDayOfWeekShort(dayCal.get(Calendar.DAY_OF_WEEK));
                String dayLabel = String.format("%s %d/%d",
                        dayOfWeek,
                        dayCal.get(Calendar.DAY_OF_MONTH),
                        dayCal.get(Calendar.MONTH) + 1);
                labels.add(dayLabel);
            }

            return hasAnyData;
        } catch (Exception e) {
            return false;
        }
    }
    private String getDayOfWeekShort(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY: return "T2";
            case Calendar.TUESDAY: return "T3";
            case Calendar.WEDNESDAY: return "T4";
            case Calendar.THURSDAY: return "T5";
            case Calendar.FRIDAY: return "T6";
            case Calendar.SATURDAY: return "T7";
            case Calendar.SUNDAY: return "CN";
            default: return "T" + dayOfWeek;
        }
    }
    private boolean generateWeeklyViewData(List<Entry> entries, List<String> labels) {
        try {
            Calendar cal = Calendar.getInstance();
            boolean hasAnyData = false;

            // Lùi về 8 tuần trước, sau đó lấy dữ liệu từ tuần cũ đến tuần mới
            for (int i = 0; i < 8; i++) {
                // Tính toán tuần
                Calendar weekCal = Calendar.getInstance();
                weekCal.add(Calendar.WEEK_OF_YEAR, -(7 - i)); // Từ 7 tuần trước đến tuần hiện tại

                int weekOfYear = weekCal.get(Calendar.WEEK_OF_YEAR);
                int year = weekCal.get(Calendar.YEAR);

                // Lấy dữ liệu
                int viewsCount = database.movieDao().getViewsCountByWeek(year, weekOfYear);

                if (viewsCount > 0) {
                    hasAnyData = true;
                }

                // Thêm vào chart
                entries.add(new Entry(i, viewsCount));

                // Tạo label đẹp hơn: "T23 (12/6)"
                weekCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // Lấy thứ 2 của tuần
                String weekLabel = String.format("T%d", weekOfYear);
                labels.add(weekLabel);
            }

            return hasAnyData;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean generateMonthlyViewData(List<Entry> entries, List<String> labels) {
        try {
            boolean hasAnyData = false;

            // Lấy 12 tháng gần nhất
            for (int i = 0; i < 12; i++) {
                Calendar monthCal = Calendar.getInstance();
                monthCal.add(Calendar.MONTH, -(11 - i)); // Từ 11 tháng trước đến tháng hiện tại

                int month = monthCal.get(Calendar.MONTH) + 1;
                int year = monthCal.get(Calendar.YEAR);

                int viewsCount = database.movieDao().getViewsCountByMonth(year, month);

                if (viewsCount > 0) {
                    hasAnyData = true;
                }

                entries.add(new Entry(i, viewsCount));

                // Tạo label đẹp: "T6/24" thay vì chỉ "T6"
                String monthLabel = String.format("T%d/%d", month, year % 100);
                labels.add(monthLabel);
            }

            return hasAnyData;
        } catch (Exception e) {
            return false;
        }
    }
    private void showNoDataChart(ChartPeriod period) {
        // Clear chart
        statisticsChart.clear();
        statisticsChart.invalidate();

        // Show no data message
        String periodText = "";
        switch (period) {
            case DAY:
                periodText = "ngày";
                break;
            case WEEK:
                periodText = "tuần";
                break;
            case MONTH:
                periodText = "tháng";
                break;
        }

        Toast.makeText(this,
                "📊 Không có dữ liệu lượt xem theo " + periodText + "\n" +
                        "💡 Hãy xem một số phim để có thống kê!",
                Toast.LENGTH_LONG).show();

        // Optionally, you can set a placeholder text on the chart
        Description description = statisticsChart.getDescription();
        description.setText("Không có dữ liệu để hiển thị");
        description.setTextSize(14f);
        description.setTextColor(getResources().getColor(android.R.color.darker_gray));
        statisticsChart.setDescription(description);
    }
    private void updateChart(List<Entry> entries, List<String> labels, ChartPeriod period) {
        if (entries.isEmpty()) {
            showNoDataChart(period);
            return;
        }

        // Check if all entries are 0
        boolean allZero = entries.stream().allMatch(entry -> entry.getY() == 0);
        if (allZero) {
            showNoDataChart(period);
            return;
        }

        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, getChartLabel(period));
        dataSet.setColor(getResources().getColor(R.color.blue));
        dataSet.setCircleColor(getResources().getColor(R.color.blue));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setFillColor(getResources().getColor(R.color.blue));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.BLACK);

        // Enable fill
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(30);

        LineData lineData = new LineData(dataSet);
        statisticsChart.setData(lineData);

        // Configure X-axis
        XAxis xAxis = statisticsChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f); // Xoay label để không bị chồng lên nhau

        // Configure Y-axis
        YAxis leftYAxis = statisticsChart.getAxisLeft();
        leftYAxis.setAxisMinimum(0f);

        // Update description
        Description description = statisticsChart.getDescription();
        description.setText("");
        statisticsChart.setDescription(description);

        // Refresh chart
        statisticsChart.animateX(1000);
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