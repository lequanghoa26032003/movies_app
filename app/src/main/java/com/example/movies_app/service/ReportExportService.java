package com.example.movies_app.service;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.movies_app.Database.AppDatabase;
import com.example.movies_app.Database.entity.Movie;
import com.example.movies_app.Database.entity.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportExportService {
    private static final String TAG = "ReportExportService";
    private final Context context;
    private final AppDatabase database;
    private final ExecutorService executorService;

    public ReportExportService(Context context) {
        this.context = context;
        this.database = AppDatabase.getInstance(context);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // Interface cho callback
    public interface ReportCallback {
        void onSuccess(String message, String filePath);
        void onError(String error);
    }

    public interface ExportCallback {
        void onSuccess(String message, String filePath);
        void onError(String error);
    }

    /**
     * Tạo báo cáo thống kê chi tiết dạng PDF/HTML
     */
    public void generateDetailedReport(ReportCallback callback) {
        executorService.execute(() -> {
            try {
                // Thu thập dữ liệu
                int totalUsers = database.userDao().getTotalUsersCount();
                int activeUsers = database.userDao().getActiveUsersCount();
                int totalMovies = database.movieDao().getTotalMoviesCount();
                int totalViews = database.movieDao().getTotalViewsFromViewCount();
                double avgRating = database.movieDao().getAverageMovieRating();
                String mostViewedMovie = database.movieDao().getMostViewedMovieFromViewCount();
                String mostActiveUser = database.userDao().getMostActiveUserWithCount();
                int newUsersThisMonth = database.userDao().getNewUsersThisMonth();
                int viewedMovies = database.movieDao().getViewedMoviesCount();

                // Tạo nội dung báo cáo HTML
                String reportContent = generateHTMLReport(
                        totalUsers, activeUsers, totalMovies, totalViews,
                        avgRating, mostViewedMovie, mostActiveUser,
                        newUsersThisMonth, viewedMovies
                );

                // Lưu báo cáo
                String fileName = "BaoCaoThongKe_" +
                        new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".html";
                String filePath = saveReportToFile(reportContent, fileName);

                callback.onSuccess("Báo cáo đã được tạo thành công!", filePath);

            } catch (Exception e) {
                Log.e(TAG, "Error generating report", e);
                callback.onError("Lỗi tạo báo cáo: " + e.getMessage());
            }
        });
    }

    /**
     * Xuất dữ liệu dạng CSV
     */
    public void exportDataToCSV(ExportCallback callback) {
        executorService.execute(() -> {
            try {
                // Tạo timestamp
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

                // Xuất dữ liệu người dùng
                String usersFileName = "Users_" + timestamp + ".csv";
                String usersFilePath = exportUsersToCSV(usersFileName);

                // Xuất dữ liệu phim
                String moviesFileName = "Movies_" + timestamp + ".csv";
                String moviesFilePath = exportMoviesToCSV(moviesFileName);

                // Xuất thống kê tổng quan
                String statsFileName = "Statistics_" + timestamp + ".csv";
                String statsFilePath = exportStatisticsToCSV(statsFileName);

                callback.onSuccess(
                        "Dữ liệu đã được xuất thành công!\n" +
                                "- " + usersFileName + "\n" +
                                "- " + moviesFileName + "\n" +
                                "- " + statsFileName,
                        usersFilePath
                );

            } catch (Exception e) {
                Log.e(TAG, "Error exporting data", e);
                callback.onError("Lỗi xuất dữ liệu: " + e.getMessage());
            }
        });
    }

    /**
     * Xuất dữ liệu dạng JSON
     */
    public void exportDataToJSON(ExportCallback callback) {
        executorService.execute(() -> {
            try {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String fileName = "MoviesApp_Data_" + timestamp + ".json";

                String jsonContent = generateJSONExport();
                String filePath = saveDataToFile(jsonContent, fileName);

                callback.onSuccess("Dữ liệu JSON đã được xuất thành công!", filePath);

            } catch (Exception e) {
                Log.e(TAG, "Error exporting JSON", e);
                callback.onError("Lỗi xuất JSON: " + e.getMessage());
            }
        });
    }

    private String generateHTMLReport(int totalUsers, int activeUsers, int totalMovies,
                                      int totalViews, double avgRating, String mostViewedMovie,
                                      String mostActiveUser, int newUsersThisMonth, int viewedMovies) {

        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <title>Báo Cáo Thống Kê Movies App</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; margin: 20px; }\n" +
                "        .header { text-align: center; color: #333; }\n" +
                "        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin: 20px 0; }\n" +
                "        .stat-card { border: 1px solid #ddd; padding: 15px; border-radius: 8px; background: #f9f9f9; }\n" +
                "        .stat-title { font-weight: bold; color: #555; margin-bottom: 10px; }\n" +
                "        .stat-value { font-size: 24px; color: #007bff; font-weight: bold; }\n" +
                "        .footer { text-align: center; margin-top: 30px; color: #666; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='header'>\n" +
                "        <h1>📊 BÁO CÁO THỐNG KÊ MOVIES APP</h1>\n" +
                "        <p>Ngày tạo: " + currentDate + "</p>\n" +
                "    </div>\n" +
                "    \n" +
                "    <div class='stats-grid'>\n" +
                "        <div class='stat-card'>\n" +
                "            <div class='stat-title'>👥 Tổng số người dùng</div>\n" +
                "            <div class='stat-value'>" + totalUsers + "</div>\n" +
                "        </div>\n" +
                "        <div class='stat-card'>\n" +
                "            <div class='stat-title'>🟢 Người dùng hoạt động</div>\n" +
                "            <div class='stat-value'>" + activeUsers + "</div>\n" +
                "        </div>\n" +
                "        <div class='stat-card'>\n" +
                "            <div class='stat-title'>🎬 Tổng số phim</div>\n" +
                "            <div class='stat-value'>" + totalMovies + "</div>\n" +
                "        </div>\n" +
                "        <div class='stat-card'>\n" +
                "            <div class='stat-title'>👁️ Tổng lượt xem</div>\n" +
                "            <div class='stat-value'>" + formatNumber(totalViews) + "</div>\n" +
                "        </div>\n" +
                "        <div class='stat-card'>\n" +
                "            <div class='stat-title'>🎯 Phim đã xem</div>\n" +
                "            <div class='stat-value'>" + viewedMovies + "</div>\n" +
                "        </div>\n" +
                "        <div class='stat-card'>\n" +
                "            <div class='stat-title'>⭐ Điểm trung bình</div>\n" +
                "            <div class='stat-value'>" + String.format("%.1f", avgRating) + "/10</div>\n" +
                "        </div>\n" +
                "        <div class='stat-card'>\n" +
                "            <div class='stat-title'>📈 Người dùng mới tháng này</div>\n" +
                "            <div class='stat-value'>" + newUsersThisMonth + "</div>\n" +
                "        </div>\n" +
                "        <div class='stat-card'>\n" +
                "            <div class='stat-title'>💡 Tỷ lệ hoạt động</div>\n" +
                "            <div class='stat-value'>" + String.format("%.1f", totalUsers > 0 ? (activeUsers * 100.0 / totalUsers) : 0) + "%</div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    \n" +
                "    <div style='margin: 30px 0;'>\n" +
                "        <h3>🏆 Thông tin nổi bật</h3>\n" +
                "        <p><strong>Phim được xem nhiều nhất:</strong> " + (mostViewedMovie != null ? mostViewedMovie : "Chưa có dữ liệu") + "</p>\n" +
                "        <p><strong>Người dùng tích cực nhất:</strong> " + (mostActiveUser != null ? mostActiveUser : "Chưa có dữ liệu") + "</p>\n" +
                "    </div>\n" +
                "    \n" +
                "    <div class='footer'>\n" +
                "        <p>Báo cáo được tạo tự động bởi Movies App</p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    private String exportUsersToCSV(String fileName) throws IOException {
        List<User> users = database.userDao().getAllUsers();

        StringBuilder csvContent = new StringBuilder();
        csvContent.append("ID,Email,Username,Full Name,Phone,Registration Date,Role,Account Status\n");

        for (User user : users) {
            csvContent.append(user.getUserId()).append(",")  // SỬA: getId() -> getUserId()
                    .append(escapeCsvField(user.getEmail())).append(",")
                    .append(escapeCsvField(user.getUsername())).append(",")
                    .append(escapeCsvField(user.getFullName())).append(",")
                    .append(escapeCsvField(user.getPhoneNumber())).append(",")
                    .append(user.getRegistrationDate()).append(",")
                    .append(user.getRole()).append(",")
                    .append(getStatusName(user.getAccountStatus())).append("\n");
        }

        return saveDataToFile(csvContent.toString(), fileName);
    }

    private String exportMoviesToCSV(String fileName) throws IOException {
        List<Movie> movies = database.movieDao().getAllMovies();

        StringBuilder csvContent = new StringBuilder();
        csvContent.append("ID,Title,Year,Country,IMDB Rating,Genres,View Count,Is Downloaded,Last Updated\n");

        for (Movie movie : movies) {
            csvContent.append(movie.getId()).append(",")
                    .append(escapeCsvField(movie.getTitle())).append(",")
                    .append(escapeCsvField(movie.getYear())).append(",")
                    .append(escapeCsvField(movie.getCountry())).append(",")
                    .append(escapeCsvField(movie.getImdbRating())).append(",")
                    .append(escapeCsvField(movie.getGenres())).append(",")
                    .append(movie.getViewCount()).append(",")
                    .append(movie.getIsDownloaded()).append(",")
                    .append(escapeCsvField(movie.getLastUpdated())).append("\n");
        }

        return saveDataToFile(csvContent.toString(), fileName);
    }

    private String exportStatisticsToCSV(String fileName) throws IOException {
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("Metric,Value\n");

        try {
            csvContent.append("Total Users,").append(database.userDao().getTotalUsersCount()).append("\n");
            csvContent.append("Active Users,").append(database.userDao().getActiveUsersCount()).append("\n");
            csvContent.append("Total Movies,").append(database.movieDao().getTotalMoviesCount()).append("\n");
            csvContent.append("Total Views,").append(database.movieDao().getTotalViewsFromViewCount()).append("\n");
            csvContent.append("Viewed Movies,").append(database.movieDao().getViewedMoviesCount()).append("\n");
            csvContent.append("Average Rating,").append(database.movieDao().getAverageMovieRating()).append("\n");
            csvContent.append("New Users This Month,").append(database.userDao().getNewUsersThisMonth()).append("\n");
        } catch (Exception e) {
            Log.e(TAG, "Error collecting statistics", e);
        }

        return saveDataToFile(csvContent.toString(), fileName);
    }

    private String generateJSONExport() {
        try {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"exportDate\": \"").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())).append("\",\n");
            json.append("  \"statistics\": {\n");
            json.append("    \"totalUsers\": ").append(database.userDao().getTotalUsersCount()).append(",\n");
            json.append("    \"activeUsers\": ").append(database.userDao().getActiveUsersCount()).append(",\n");
            json.append("    \"totalMovies\": ").append(database.movieDao().getTotalMoviesCount()).append(",\n");
            json.append("    \"totalViews\": ").append(database.movieDao().getTotalViewsFromViewCount()).append(",\n");
            json.append("    \"viewedMovies\": ").append(database.movieDao().getViewedMoviesCount()).append(",\n");
            json.append("    \"averageRating\": ").append(database.movieDao().getAverageMovieRating()).append(",\n");
            json.append("    \"newUsersThisMonth\": ").append(database.userDao().getNewUsersThisMonth()).append("\n");
            json.append("  },\n");

            // Export users data
            List<User> users = database.userDao().getAllUsers();
            json.append("  \"users\": [\n");
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                json.append("    {\n");
                json.append("      \"id\": ").append(user.getUserId()).append(",\n");  // SỬA: getId() -> getUserId()
                json.append("      \"email\": \"").append(escapeJson(user.getEmail())).append("\",\n");
                json.append("      \"username\": \"").append(escapeJson(user.getUsername())).append("\",\n");
                json.append("      \"fullName\": \"").append(escapeJson(user.getFullName())).append("\",\n");
                json.append("      \"phoneNumber\": \"").append(escapeJson(user.getPhoneNumber())).append("\",\n");
                json.append("      \"registrationDate\": \"").append(user.getRegistrationDate()).append("\",\n");
                json.append("      \"role\": \"").append(user.getRole()).append("\",\n");
                json.append("      \"accountStatus\": ").append(user.getAccountStatus()).append("\n");
                json.append("    }");
                if (i < users.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("  ],\n");

            // Export movies data (limited to first 100 for performance)
            List<Movie> movies = database.movieDao().getAllMovies();
            if (movies.size() > 100) {
                movies = movies.subList(0, 100);
            }

            json.append("  \"movies\": [\n");
            for (int i = 0; i < movies.size(); i++) {
                Movie movie = movies.get(i);
                json.append("    {\n");
                json.append("      \"id\": ").append(movie.getId()).append(",\n");
                json.append("      \"title\": \"").append(escapeJson(movie.getTitle())).append("\",\n");
                json.append("      \"year\": \"").append(escapeJson(movie.getYear())).append("\",\n");
                json.append("      \"country\": \"").append(escapeJson(movie.getCountry())).append("\",\n");
                json.append("      \"genres\": \"").append(escapeJson(movie.getGenres())).append("\",\n");
                json.append("      \"imdbRating\": \"").append(escapeJson(movie.getImdbRating())).append("\",\n");
                json.append("      \"viewCount\": ").append(movie.getViewCount()).append("\n");
                json.append("    }");
                if (i < movies.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("  ]\n");
            json.append("}");

            return json.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error generating JSON", e);
            return "{\"error\": \"Failed to generate JSON export\"}";
        }
    }

    private String saveReportToFile(String content, String fileName) throws IOException {
        return saveDataToFile(content, fileName);
    }

    private String saveDataToFile(String content, String fileName) throws IOException {
        File documentsDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "MoviesApp");
        if (!documentsDir.exists()) {
            documentsDir.mkdirs();
        }

        File file = new File(documentsDir, fileName);
        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.close();

        return file.getAbsolutePath();
    }

    private String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String getStatusName(int status) {
        switch (status) {
            case 1: return "Active";
            case 2: return "Blocked";
            case 0: return "Inactive";
            default: return "Unknown";
        }
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

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}