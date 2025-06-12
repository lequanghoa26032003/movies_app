package com.example.movies_app.Database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.example.movies_app.Database.entity.FavoriteMovie;
import com.example.movies_app.Database.entity.Movie;
import com.example.movies_app.Database.entity.MovieDetail;
import com.example.movies_app.Database.entity.SearchHistory;
import com.example.movies_app.Database.entity.WatchHistory;

@Dao
public interface MovieDao {
    // Các phương thức để chèn dữ liệu mẫu
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

    // Các phương thức khác của MovieDao...
}