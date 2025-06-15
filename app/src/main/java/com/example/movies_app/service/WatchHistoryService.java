package com.example.movies_app.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.movies_app.Database.AppDatabase;
import com.example.movies_app.Database.dao.WatchHistoryDao;
import com.example.movies_app.Database.entity.HistoryEntry;
import com.example.movies_app.Database.entity.WatchHistory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WatchHistoryService {
    private static final String TAG = "WatchHistoryService";
    private static WatchHistoryService instance;
    private final Context context;
    private final WatchHistoryDao watchHistoryDao;
    private final ExecutorService executorService;
    private final SimpleDateFormat dateFormat;

    private WatchHistoryService(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase database = AppDatabase.getInstance(this.context);
        this.watchHistoryDao = database.watchHistoryDao();
        this.executorService = Executors.newSingleThreadExecutor();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

    public static synchronized WatchHistoryService getInstance(Context context) {
        if (instance == null) {
            instance = new WatchHistoryService(context);
        }
        return instance;
    }

    // Interface cho callback
    public interface WatchHistoryCallback {
        void onSuccess(List<HistoryEntry> historyEntries);
        void onError(String error);
    }

    public interface WatchHistoryOperationCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    // Lưu lịch sử xem phim
    public void saveWatchHistory(int movieId, long position, WatchHistoryOperationCallback callback) {
        int userId = getCurrentUserId();
        if (userId == -1) {
            if (callback != null) {
                callback.onError("Người dùng chưa đăng nhập");
            }
            return;
        }

        executorService.execute(() -> {
            try {
                String currentDate = dateFormat.format(new Date());
                
                // Kiểm tra xem đã có lịch sử cho phim này chưa
                WatchHistory existingHistory = watchHistoryDao.getWatchHistoryByUserAndMovie(userId, movieId);
                
                if (existingHistory != null) {
                    // Cập nhật lịch sử hiện có
                    watchHistoryDao.updateWatchPosition(userId, movieId, position, currentDate);
                    Log.d(TAG, "Updated watch history for movie " + movieId + " at position " + position);
                } else {
                    // Tạo lịch sử mới
                    WatchHistory newHistory = new WatchHistory(userId, movieId, currentDate, position);
                    watchHistoryDao.insertWatchHistory(newHistory);
                    Log.d(TAG, "Created new watch history for movie " + movieId + " at position " + position);
                }
                
                if (callback != null) {
                    callback.onSuccess("Đã lưu lịch sử xem");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving watch history", e);
                if (callback != null) {
                    callback.onError("Lỗi khi lưu lịch sử: " + e.getMessage());
                }
            }
        });
    }

    // Lấy lịch sử xem theo người dùng
    public void getWatchHistory(WatchHistoryCallback callback) {
        int userId = getCurrentUserId();
        if (userId == -1) {
            if (callback != null) {
                callback.onError("Người dùng chưa đăng nhập");
            }
            return;
        }

        executorService.execute(() -> {
            try {
                List<HistoryEntry> historyEntries = watchHistoryDao.getWatchHistoryWithMoviesByUser(userId);
                Log.d(TAG, "Retrieved " + historyEntries.size() + " history entries for user " + userId);
                
                if (callback != null) {
                    callback.onSuccess(historyEntries);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting watch history", e);
                if (callback != null) {
                    callback.onError("Lỗi khi tải lịch sử: " + e.getMessage());
                }
            }
        });
    }

    // Xóa lịch sử xem một phim
    public void removeWatchHistory(int movieId, WatchHistoryOperationCallback callback) {
        int userId = getCurrentUserId();
        if (userId == -1) {
            if (callback != null) {
                callback.onError("Người dùng chưa đăng nhập");
            }
            return;
        }

        executorService.execute(() -> {
            try {
                watchHistoryDao.deleteWatchHistoryByUserAndMovie(userId, movieId);
                Log.d(TAG, "Removed watch history for movie " + movieId);
                
                if (callback != null) {
                    callback.onSuccess("Đã xóa khỏi lịch sử");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error removing watch history", e);
                if (callback != null) {
                    callback.onError("Lỗi khi xóa lịch sử: " + e.getMessage());
                }
            }
        });
    }

    // Xóa toàn bộ lịch sử xem
    public void clearAllWatchHistory(WatchHistoryOperationCallback callback) {
        int userId = getCurrentUserId();
        if (userId == -1) {
            if (callback != null) {
                callback.onError("Người dùng chưa đăng nhập");
            }
            return;
        }

        executorService.execute(() -> {
            try {
                watchHistoryDao.deleteAllWatchHistoryByUser(userId);
                Log.d(TAG, "Cleared all watch history for user " + userId);
                
                if (callback != null) {
                    callback.onSuccess("Đã xóa toàn bộ lịch sử");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error clearing watch history", e);
                if (callback != null) {
                    callback.onError("Lỗi khi xóa lịch sử: " + e.getMessage());
                }
            }
        });
    }

    // Lấy vị trí xem gần nhất của một phim
    public void getLastWatchPosition(int movieId, LastPositionCallback callback) {
        int userId = getCurrentUserId();
        if (userId == -1) {
            if (callback != null) {
                callback.onResult(0); // Trả về 0 nếu chưa đăng nhập
            }
            return;
        }

        executorService.execute(() -> {
            try {
                WatchHistory history = watchHistoryDao.getWatchHistoryByUserAndMovie(userId, movieId);
                long position = history != null ? history.getPosition() : 0;
                
                if (callback != null) {
                    callback.onResult(position);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting last watch position", e);
                if (callback != null) {
                    callback.onResult(0);
                }
            }
        });
    }

    public interface LastPositionCallback {
        void onResult(long position);
    }

    // Helper method để lấy userId từ SharedPreferences
    private int getCurrentUserId() {
        SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }

    // Cleanup resources
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}