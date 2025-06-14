package com.example.movies_app.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.movies_app.Database.AppDatabase;
import com.example.movies_app.Database.entity.Movie;
import com.example.movies_app.Database.entity.MovieDetail;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovieService {
    private static MovieService instance;
    private AppDatabase database;
    private ExecutorService executor;
    private Handler mainHandler;

    public interface MovieCallback {
        void onSuccess(List<Movie> movies);
        void onError(String error);
    }

    public interface MovieDetailCallback {
        void onSuccess(Movie movie, MovieDetail movieDetail);
        void onError(String error);
    }

    private MovieService(Context context) {
        database = AppDatabase.getInstance(context);
        executor = Executors.newFixedThreadPool(4);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized MovieService getInstance(Context context) {
        if (instance == null) {
            instance = new MovieService(context.getApplicationContext());
        }
        return instance;
    }

    // Lấy tất cả phim
    public void getAllMovies(MovieCallback callback) {
        executor.execute(() -> {
            try {
                List<Movie> movies = database.movieDao().getAllMovies();
                mainHandler.post(() -> callback.onSuccess(movies));
            } catch (Exception e) {
                Log.e("MovieService", "Error getting all movies", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // Tìm kiếm phim
    public void searchMovies(String query, MovieCallback callback) {
        executor.execute(() -> {
            try {
                String searchQuery = "%" + query + "%";
                List<Movie> movies = database.movieDao().searchMovies(searchQuery);
                mainHandler.post(() -> callback.onSuccess(movies));
            } catch (Exception e) {
                Log.e("MovieService", "Error searching movies", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // Lấy phim theo thể loại
    public void getMoviesByGenre(String genre, MovieCallback callback) {
        executor.execute(() -> {
            try {
                List<Movie> allMovies = database.movieDao().getAllMovies();
                List<Movie> filteredMovies = allMovies.stream()
                    .filter(movie -> movie.getGenres() != null && 
                           movie.getGenres().toLowerCase().contains(genre.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
                
                mainHandler.post(() -> callback.onSuccess(filteredMovies));
            } catch (Exception e) {
                Log.e("MovieService", "Error getting movies by genre", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // Lấy phim phổ biến (sắp xếp theo viewCount)
    public void getPopularMovies(MovieCallback callback) {
        executor.execute(() -> {
            try {
                List<Movie> movies = database.movieDao().getAllMovies();
                // Sắp xếp theo viewCount giảm dần
                movies.sort((m1, m2) -> Integer.compare(m2.getViewCount(), m1.getViewCount()));
                
                // Lấy top 20 phim phổ biến
                List<Movie> popularMovies = movies.stream()
                    .limit(20)
                    .collect(java.util.stream.Collectors.toList());
                
                mainHandler.post(() -> callback.onSuccess(popularMovies));
            } catch (Exception e) {
                Log.e("MovieService", "Error getting popular movies", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // Lấy phim mới nhất
    public void getLatestMovies(MovieCallback callback) {
        executor.execute(() -> {
            try {
                List<Movie> movies = database.movieDao().getAllMovies();
                // Sắp xếp theo lastUpdated giảm dần
                movies.sort((m1, m2) -> {
                    if (m1.getLastUpdated() == null) return 1;
                    if (m2.getLastUpdated() == null) return -1;
                    return m2.getLastUpdated().compareTo(m1.getLastUpdated());
                });
                
                // Lấy top 20 phim mới nhất
                List<Movie> latestMovies = movies.stream()
                    .limit(20)
                    .collect(java.util.stream.Collectors.toList());
                
                mainHandler.post(() -> callback.onSuccess(latestMovies));
            } catch (Exception e) {
                Log.e("MovieService", "Error getting latest movies", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // Lấy chi tiết phim
    public void getMovieDetail(int movieId, MovieDetailCallback callback) {
        executor.execute(() -> {
            try {
                Movie movie = database.movieDao().getMovieById(movieId);
                MovieDetail movieDetail = database.movieDao().getMovieDetailById(movieId);
                
                if (movie != null) {
                    // Tăng view count
                    movie.incrementViewCount();
                    database.movieDao().updateMovie(movie);
                }
                
                mainHandler.post(() -> {
                    if (movie != null) {
                        callback.onSuccess(movie, movieDetail);
                    } else {
                        callback.onError("Movie not found");
                    }
                });
            } catch (Exception e) {
                Log.e("MovieService", "Error getting movie detail", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // Lấy phim theo năm
    public void getMoviesByYear(String year, MovieCallback callback) {
        executor.execute(() -> {
            try {
                List<Movie> allMovies = database.movieDao().getAllMovies();
                List<Movie> filteredMovies = allMovies.stream()
                    .filter(movie -> movie.getYear() != null && movie.getYear().equals(year))
                    .collect(java.util.stream.Collectors.toList());
                
                mainHandler.post(() -> callback.onSuccess(filteredMovies));
            } catch (Exception e) {
                Log.e("MovieService", "Error getting movies by year", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // Lấy phim theo quốc gia
    public void getMoviesByCountry(String country, MovieCallback callback) {
        executor.execute(() -> {
            try {
                List<Movie> allMovies = database.movieDao().getAllMovies();
                List<Movie> filteredMovies = allMovies.stream()
                    .filter(movie -> movie.getCountry() != null && 
                           movie.getCountry().toLowerCase().contains(country.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
                
                mainHandler.post(() -> callback.onSuccess(filteredMovies));
            } catch (Exception e) {
                Log.e("MovieService", "Error getting movies by country", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
}