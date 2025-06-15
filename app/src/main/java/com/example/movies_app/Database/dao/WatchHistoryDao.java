package com.example.movies_app.Database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.movies_app.Database.entity.HistoryEntry;
import com.example.movies_app.Database.entity.WatchHistory;

import java.util.List;

@Dao
public interface WatchHistoryDao {

    // ========== INSERT METHODS ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWatchHistory(WatchHistory watchHistory);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWatchHistories(List<WatchHistory> watchHistories);

    // ========== SELECT METHODS ==========
    @Query("SELECT * FROM watch_history WHERE userId = :userId ORDER BY watchDate DESC")
    List<WatchHistory> getWatchHistoryByUser(int userId);

    @Query("SELECT * FROM watch_history WHERE userId = :userId AND movieId = :movieId LIMIT 1")
    WatchHistory getWatchHistoryByUserAndMovie(int userId, int movieId);

    // Query JOIN để lấy thông tin phim cùng với lịch sử xem
    @Query("SELECT m.*, wh.watchDate FROM movies m " +
           "INNER JOIN watch_history wh ON m.id = wh.movieId " +
           "WHERE wh.userId = :userId " +
           "ORDER BY wh.watchDate DESC")
    List<HistoryEntry> getWatchHistoryWithMoviesByUser(int userId);

    @Query("SELECT COUNT(*) FROM watch_history WHERE userId = :userId")
    int getWatchHistoryCountByUser(int userId);

    @Query("SELECT COUNT(*) FROM watch_history WHERE userId = :userId AND movieId = :movieId")
    int checkWatchHistoryExists(int userId, int movieId);

    // ========== UPDATE METHODS ==========
    @Update
    void updateWatchHistory(WatchHistory watchHistory);

    @Query("UPDATE watch_history SET position = :position, watchDate = :watchDate " +
           "WHERE userId = :userId AND movieId = :movieId")
    void updateWatchPosition(int userId, int movieId, long position, String watchDate);

    // ========== DELETE METHODS ==========
    @Delete
    void deleteWatchHistory(WatchHistory watchHistory);

    @Query("DELETE FROM watch_history WHERE userId = :userId AND movieId = :movieId")
    void deleteWatchHistoryByUserAndMovie(int userId, int movieId);

    @Query("DELETE FROM watch_history WHERE userId = :userId")
    void deleteAllWatchHistoryByUser(int userId);

    @Query("DELETE FROM watch_history WHERE movieId = :movieId")
    void deleteWatchHistoryByMovie(int movieId);

    // ========== UTILITY METHODS ==========
    @Query("SELECT * FROM watch_history WHERE userId = :userId ORDER BY watchDate DESC LIMIT :limit")
    List<WatchHistory> getRecentWatchHistory(int userId, int limit);

    @Query("SELECT DISTINCT movieId FROM watch_history WHERE userId = :userId ORDER BY watchDate DESC")
    List<Integer> getWatchedMovieIds(int userId);
}