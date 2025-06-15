package com.example.movies_app.Database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.movies_app.Database.entity.FavoriteMovie;
import com.example.movies_app.Database.entity.Movie;
import com.example.movies_app.Database.entity.MovieDetail;
import com.example.movies_app.Database.entity.SearchHistory;
import com.example.movies_app.Database.entity.WatchHistory;

import java.util.List;

@Dao
public interface MovieDao {
    // Các phương thức cũ...
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMovie(Movie movie);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMovieDetail(MovieDetail movieDetail);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavoriteMovie(FavoriteMovie favoriteMovie);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWatchHistory(WatchHistory watchHistory);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSearchHistory(SearchHistory searchHistory);

    // Các phương thức SELECT cũ...
    @Query("SELECT * FROM movies")
    List<Movie> getAllMovies();

    @Query("SELECT * FROM movies WHERE title LIKE :searchQuery")
    List<Movie> searchMovies(String searchQuery);

    @Query("SELECT * FROM movies WHERE title = :title")
    List<Movie> searchMoviesByTitle(String title);

    @Query("SELECT * FROM movies WHERE id = :id")
    Movie getMovieById(int id);

    @Query("SELECT * FROM movie_details WHERE movieId = :id")
    MovieDetail getMovieDetailById(int id);

    @Query("SELECT * FROM movie_details")
    List<MovieDetail> getAllMovieDetails();

    @Query("SELECT * FROM favorite_movies WHERE userId = :userId")
    List<FavoriteMovie> getFavoriteMoviesByUser(int userId);

    @Query("SELECT * FROM watch_history WHERE userId = :userId ORDER BY watchDate DESC")
    List<WatchHistory> getWatchHistoryByUser(int userId);

    @Query("SELECT * FROM search_history WHERE userId = :userId ORDER BY searchDate DESC LIMIT 10")
    List<SearchHistory> getRecentSearches(int userId);

    // Các phương thức UPDATE cũ...
    @Update
    void updateMovie(Movie movie);

    @Update
    void updateMovieDetail(MovieDetail movieDetail);

    @Update
    void updateFavoriteMovie(FavoriteMovie favoriteMovie);

    @Update
    void updateWatchHistory(WatchHistory watchHistory);

    // Các phương thức DELETE cũ...
    @Delete
    void deleteMovie(Movie movie);

    @Delete
    void deleteMovieDetail(MovieDetail movieDetail);

    @Delete
    void deleteFavoriteMovie(FavoriteMovie favoriteMovie);

    @Delete
    void deleteWatchHistory(WatchHistory watchHistory);

    @Delete
    void deleteSearchHistory(SearchHistory searchHistory);

    @Query("DELETE FROM movies WHERE id = :movieId")
    void deleteMovieById(int movieId);

    @Query("DELETE FROM movie_details WHERE movieId = :movieId")
    void deleteMovieDetailById(int movieId);

    @Query("DELETE FROM favorite_movies WHERE movieId = :movieId AND userId = :userId")
    void deleteFavoriteMovieByIds(int movieId, int userId);

    @Query("DELETE FROM watch_history WHERE movieId = :movieId AND userId = :userId")
    void deleteWatchHistoryByIds(int movieId, int userId);

    // Kiểm tra sự tồn tại
    @Query("SELECT COUNT(*) FROM movies WHERE id = :movieId")
    int checkMovieExists(int movieId);

    @Query("SELECT COUNT(*) FROM favorite_movies WHERE movieId = :movieId AND userId = :userId")
    int checkFavoriteExists(int movieId, int userId);

    // ========== THỐNG KÊ CŨ (giữ nguyên để tương thích) ==========
    @Query("SELECT COUNT(*) FROM movies")
    int getTotalMoviesCount();

    @Query("SELECT COUNT(*) FROM favorite_movies WHERE userId = :userId")
    int getUserFavoritesCount(int userId);

    @Query("SELECT COUNT(*) FROM watch_history WHERE userId = :userId")
    int getUserWatchHistoryCount(int userId);

    @Query("SELECT COUNT(*) FROM watch_history")
    int getTotalViewsCount();

    @Query("SELECT m.title || ' (' || COUNT(wh.movieId) || ' lượt xem)' FROM movies m " +
            "LEFT JOIN watch_history wh ON m.id = wh.movieId " +
            "GROUP BY m.id, m.title " +
            "ORDER BY COUNT(wh.movieId) DESC LIMIT 1")
    String getMostViewedMovieWithCount();

    @Query("SELECT COUNT(*) FROM favorite_movies")
    int getTotalFavoritesCount();

    @Query("SELECT COUNT(DISTINCT userId) FROM watch_history " +
            "WHERE watchDate >= date('now', '-30 days')")
    int getActiveViewersLastMonth();

    @Query("SELECT COUNT(*) FROM movies WHERE lastUpdated >= date('now', '-30 days')")
    int getMoviesAddedLastMonth();

    @Query("SELECT AVG(CAST(imdbRating as REAL)) FROM movies WHERE imdbRating IS NOT NULL AND imdbRating != ''")
    double getAverageMovieRating();

    // ========== THỐNG KÊ MỚI SỬ DỤNG TRƯỜNG viewCount ==========

    // Cập nhật view count cho một phim
    @Query("UPDATE movies SET viewCount = viewCount + 1 WHERE id = :movieId")
    void incrementMovieViewCount(int movieId);

    // Lấy tổng số lượt xem từ trường viewCount
    @Query("SELECT SUM(viewCount) FROM movies")
    int getTotalViewsFromViewCount();

    // Lấy phim có lượt xem cao nhất
    @Query("SELECT title || ' (' || viewCount || ' lượt xem)' FROM movies " +
            "WHERE viewCount = (SELECT MAX(viewCount) FROM movies) LIMIT 1")
    String getMostViewedMovieFromViewCount();

    // Lấy top phim có lượt xem cao nhất
    @Query("SELECT * FROM movies ORDER BY viewCount DESC LIMIT :limit")
    List<Movie> getTopViewedMovies(int limit);

    // Lấy phim có lượt xem thấp nhất (> 0)
    @Query("SELECT * FROM movies WHERE viewCount > 0 ORDER BY viewCount ASC LIMIT :limit")
    List<Movie> getLeastViewedMovies(int limit);

    // Lấy trung bình lượt xem
    @Query("SELECT AVG(viewCount) FROM movies WHERE viewCount > 0")
    double getAverageViewCount();

    // Lấy tổng số phim đã được xem (viewCount > 0)
    @Query("SELECT COUNT(*) FROM movies WHERE viewCount > 0")
    int getViewedMoviesCount();

    // Lấy tổng số phim chưa được xem (viewCount = 0)
    @Query("SELECT COUNT(*) FROM movies WHERE viewCount = 0")
    int getUnwatchedMoviesCount();

    // Thống kê theo thể loại (genres)
    @Query("SELECT genres, SUM(viewCount) as totalViews FROM movies " +
            "WHERE viewCount > 0 GROUP BY genres ORDER BY totalViews DESC")
    List<GenreViewCount> getViewCountByGenre();

    // Thống kê theo năm
    @Query("SELECT year, SUM(viewCount) as totalViews FROM movies " +
            "WHERE viewCount > 0 GROUP BY year ORDER BY totalViews DESC")
    List<YearViewCount> getViewCountByYear();

    // Đặt lại view count cho một phim
    @Query("UPDATE movies SET viewCount = :count WHERE id = :movieId")
    void setMovieViewCount(int movieId, int count);

    // Đặt lại tất cả view count về 0
    @Query("UPDATE movies SET viewCount = 0")
    void resetAllViewCounts();

    // Class helper cho thống kê
    class GenreViewCount {
        public String genres;
        public int totalViews;
    }

    class YearViewCount {
        public String year;
        public int totalViews;
    }
    @Query("SELECT DISTINCT genres FROM movies WHERE genres IS NOT NULL AND genres != ''")
    List<String> getAllGenres();

    @Query("SELECT genres FROM movies WHERE genres IS NOT NULL AND genres != '' GROUP BY genres")
    List<String> getUniqueGenres();
    @Query("SELECT m.* FROM movies m " +
            "INNER JOIN favorite_movies f ON m.id = f.movieId " +
            "WHERE f.userId = :userId " +
            "ORDER BY f.dateAdded DESC")
    List<Movie> getFavoriteMoviesWithDetails(int userId);

    @Query("DELETE FROM favorite_movies WHERE movieId = :movieId AND userId = :userId")
    void removeFavoriteMovie(int movieId, int userId);

    @Query("SELECT COUNT(*) > 0 FROM favorite_movies WHERE movieId = :movieId AND userId = :userId")
    boolean isFavoriteMovie(int movieId, int userId);
    @Query("SELECT m.* FROM movies m " +
            "INNER JOIN favorite_movies fm ON m.id = fm.movieId " +
            "WHERE fm.userId = :userId " +
            "ORDER BY fm.dateAdded DESC")
    List<Movie> getFavoriteMoviesWithDetailsByUser(int userId);

    @Query("SELECT COUNT(*) FROM watch_history WHERE userId = :userId")
    int getWatchHistoryCountByUser(int userId);

    @Query("SELECT COUNT(*) FROM favorite_movies WHERE userId = :userId")
    int getFavoriteMoviesCountByUser(int userId);

}