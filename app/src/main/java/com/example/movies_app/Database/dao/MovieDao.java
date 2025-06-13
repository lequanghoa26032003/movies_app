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
    // Các phương thức để chèn dữ liệu
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

    // Các phương thức SELECT - lấy dữ liệu (Sửa tên bảng)
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

    // Các phương thức UPDATE - cập nhật dữ liệu
    @Update
    void updateMovie(Movie movie);

    @Update
    void updateMovieDetail(MovieDetail movieDetail);

    @Update
    void updateFavoriteMovie(FavoriteMovie favoriteMovie);

    @Update
    void updateWatchHistory(WatchHistory watchHistory);

    // Các phương thức DELETE - xóa dữ liệu
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

    // Các phương thức DELETE với ID (Sửa tên bảng)
    @Query("DELETE FROM movies WHERE id = :movieId")
    void deleteMovieById(int movieId);

    @Query("DELETE FROM movie_details WHERE movieId = :movieId")
    void deleteMovieDetailById(int movieId);

    @Query("DELETE FROM favorite_movies WHERE movieId = :movieId AND userId = :userId")
    void deleteFavoriteMovieByIds(int movieId, int userId);

    @Query("DELETE FROM watch_history WHERE movieId = :movieId AND userId = :userId")
    void deleteWatchHistoryByIds(int movieId, int userId);

    // Kiểm tra sự tồn tại (Sửa tên bảng)
    @Query("SELECT COUNT(*) FROM movies WHERE id = :movieId")
    int checkMovieExists(int movieId);

    @Query("SELECT COUNT(*) FROM favorite_movies WHERE movieId = :movieId AND userId = :userId")
    int checkFavoriteExists(int movieId, int userId);

    // Thống kê (Sửa tên bảng)
    @Query("SELECT COUNT(*) FROM movies")
    int getTotalMoviesCount();

    @Query("SELECT COUNT(*) FROM favorite_movies WHERE userId = :userId")
    int getUserFavoritesCount(int userId);

    @Query("SELECT COUNT(*) FROM watch_history WHERE userId = :userId")
    int getUserWatchHistoryCount(int userId);
}