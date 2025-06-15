package com.example.movies_app.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.movies_app.Database.AppDatabase;
import com.example.movies_app.Database.entity.FavoriteMovie;
import com.example.movies_app.Database.entity.Movie;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoriteService {
    private static final String TAG = "FavoriteService";
    private static FavoriteService instance;
    private AppDatabase database;
    private ExecutorService executor;
    private Handler mainHandler;

    public interface FavoriteCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface FavoriteCheckCallback {
        void onResult(boolean isFavorite);
        void onError(String error);
    }

    public interface FavoriteListCallback {
        void onSuccess(List<Movie> movies);
        void onError(String error);
    }

    private FavoriteService(Context context) {
        database = AppDatabase.getInstance(context);
        initializeExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    private void initializeExecutor() {
        executor = Executors.newFixedThreadPool(4);
    }

    public static synchronized FavoriteService getInstance(Context context) {
        if (instance == null) {
            instance = new FavoriteService(context.getApplicationContext());
        }
        return instance;
    }

    // Kiểm tra và tái tạo executor nếu cần
    private void ensureExecutorReady() {
        if (executor == null || executor.isShutdown() || executor.isTerminated()) {
            Log.d(TAG, "Executor is shutdown, creating new one");
            initializeExecutor();
        }
    }

    // Thực thi task với kiểm tra executor
    private void executeTask(Runnable task) {
        try {
            ensureExecutorReady();
            executor.execute(task);
        } catch (Exception e) {
            Log.e(TAG, "Error executing task, recreating executor", e);
            initializeExecutor();
            try {
                executor.execute(task);
            } catch (Exception retryException) {
                Log.e(TAG, "Failed to execute task even after recreating executor", retryException);
            }
        }
    }

    // Thêm phim vào yêu thích
    public void addToFavorites(int userId, int movieId, FavoriteCallback callback) {
        executeTask(() -> {
            try {
                // Kiểm tra xem đã tồn tại chưa
                if (database.movieDao().checkFavoriteExists(movieId, userId) > 0) {
                    mainHandler.post(() -> callback.onError("Phim đã có trong danh sách yêu thích"));
                    return;
                }

                // Thêm mới
                String currentDate = getCurrentDate();
                FavoriteMovie favorite = new FavoriteMovie(userId, movieId, currentDate);
                database.movieDao().insertFavoriteMovie(favorite);

                mainHandler.post(() -> callback.onSuccess("Đã thêm vào danh sách yêu thích"));

            } catch (Exception e) {
                Log.e(TAG, "Error adding to favorites", e);
                mainHandler.post(() -> callback.onError("Lỗi khi thêm vào yêu thích: " + e.getMessage()));
            }
        });
    }

    // Xóa phim khỏi yêu thích
    public void removeFromFavorites(int userId, int movieId, FavoriteCallback callback) {
        executeTask(() -> {
            try {
                database.movieDao().removeFavoriteMovie(movieId, userId);
                mainHandler.post(() -> callback.onSuccess("Đã xóa khỏi danh sách yêu thích"));

            } catch (Exception e) {
                Log.e(TAG, "Error removing from favorites", e);
                mainHandler.post(() -> callback.onError("Lỗi khi xóa khỏi yêu thích: " + e.getMessage()));
            }
        });
    }

    // Kiểm tra trạng thái yêu thích
    public void checkFavoriteStatus(int userId, int movieId, FavoriteCheckCallback callback) {
        executeTask(() -> {
            try {
                boolean isFavorite = database.movieDao().checkFavoriteExists(movieId, userId) > 0;
                mainHandler.post(() -> callback.onResult(isFavorite));

            } catch (Exception e) {
                Log.e(TAG, "Error checking favorite status", e);
                mainHandler.post(() -> callback.onError("Lỗi khi kiểm tra trạng thái yêu thích"));
            }
        });
    }

    // Lấy danh sách phim yêu thích
    public void getFavoriteMovies(int userId, FavoriteListCallback callback) {
        executeTask(() -> {
            try {
                List<Movie> favoriteMovies = database.movieDao().getFavoriteMoviesWithDetails(userId);
                mainHandler.post(() -> callback.onSuccess(favoriteMovies));

            } catch (Exception e) {
                Log.e(TAG, "Error getting favorite movies", e);
                mainHandler.post(() -> callback.onError("Lỗi khi lấy danh sách yêu thích: " + e.getMessage()));
            }
        });
    }

    // Toggle trạng thái yêu thích
    public void toggleFavorite(int userId, int movieId, FavoriteCallback callback) {
        executeTask(() -> {
            try {
                boolean isFavorite = database.movieDao().checkFavoriteExists(movieId, userId) > 0;

                if (isFavorite) {
                    // Xóa khỏi yêu thích
                    database.movieDao().removeFavoriteMovie(movieId, userId);
                    mainHandler.post(() -> callback.onSuccess("Đã xóa khỏi danh sách yêu thích"));
                } else {
                    // Thêm vào yêu thích
                    String currentDate = getCurrentDate();
                    FavoriteMovie favorite = new FavoriteMovie(userId, movieId, currentDate);
                    database.movieDao().insertFavoriteMovie(favorite);
                    mainHandler.post(() -> callback.onSuccess("Đã thêm vào danh sách yêu thích"));
                }

            } catch (Exception e) {
                Log.e(TAG, "Error toggling favorite", e);
                mainHandler.post(() -> callback.onError("Lỗi khi cập nhật yêu thích: " + e.getMessage()));
            }
        });
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    // Thêm method để reset instance nếu cần
    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.shutdown();
            instance = null;
        }
    }
}