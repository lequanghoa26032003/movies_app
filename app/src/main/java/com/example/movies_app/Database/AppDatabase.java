package com.example.movies_app.Database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.movies_app.Database.entity.*;
import com.example.movies_app.Database.dao.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {
                User.class,
                UserPreference.class,
                Movie.class,
                MovieDetail.class,
                FavoriteMovie.class,
                WatchHistory.class,
                SearchHistory.class
        },
        version = 4, // ✅ TĂNG VERSION LÊN 4
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "movies_app.db";
    private static AppDatabase instance;

    // Tạo Executor để chạy database operations trên thread khác
    private static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    public abstract MovieDao movieDao();
    public abstract UserDao userDao();
    public abstract WatchHistoryDao watchHistoryDao(); // ✅ THÊM DAO MỚI

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .addCallback(prepopulateDatabase)
                    .build();
        }
        return instance;
    }

    // ✅ THÊM METHOD GETTER CHO EXECUTOR
    public static ExecutorService getDatabaseWriteExecutor() {
        return databaseWriteExecutor;
    }

    // Callback để chèn dữ liệu mẫu khi database được tạo
    private static final RoomDatabase.Callback prepopulateDatabase = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // Chèn dữ liệu mẫu trên thread khác
            databaseWriteExecutor.execute(() -> {
                // Lấy các DAO
                UserDao userDao = instance.userDao();
                MovieDao movieDao = instance.movieDao();
                WatchHistoryDao watchHistoryDao = instance.watchHistoryDao(); // ✅ THÊM DAO MỚI

                // Thêm dữ liệu mẫu cho người dùng
                User adminUser = new User(
                        "admin@example.com",
                        "admin",
                        "5f4dcc3b5aa765d61d8327deb882cf99", // md5 của "password"
                        "Administrator",
                        "0987654321",
                        "2025-06-15 11:18:34" // ✅ CẬP NHẬT THỜI GIAN HIỆN TẠI
                );
                adminUser.setAvatarUrl("https://example.com/avatars/admin.jpg");
                long adminId = userDao.insertUser(adminUser);

                User normalUser = new User(
                        "user@example.com",
                        "user123",
                        "5f4dcc3b5aa765d61d8327deb882cf99", // md5 của "password"
                        "Normal User",
                        "0123456789",
                        "2025-06-15 11:18:34" // ✅ CẬP NHẬT THỜI GIAN HIỆN TẠI
                );
                long userId = userDao.insertUser(normalUser);

                // Thêm tùy chọn người dùng
                UserPreference adminPrefs = new UserPreference((int)adminId);
                adminPrefs.setDarkMode(true);
                userDao.insertUserPreference(adminPrefs);

                UserPreference userPrefs = new UserPreference((int)userId);
                userDao.insertUserPreference(userPrefs);

                // ✅ THÊM PHIM MẪU VỚI INT (0 = CHƯA TẢI, 1 = ĐÃ TẢI)
                Movie movie1 = new Movie(
                        1,
                        "The Shawshank Redemption",
                        "https://m.media-amazon.com/images/M/MV5BNDE3ODcxYzMtY2YzZC00NmNlLWJiNDMtZDViZWM2MzIxZDYwXkEyXkFqcGdeQXVyNjAwNDUxODI@._V1_SX300.jpg",
                        "1994",
                        "USA",
                        "9.3",
                        "Drama",
                        "https://example.com/images/shawshank1.jpg,https://example.com/images/shawshank2.jpg",
                        "2025-06-15 11:18:34",
                        0 // ✅ INT - 0 = CHƯA TẢI
                );
                movieDao.insertMovie(movie1);

                Movie movie2 = new Movie(
                        2,
                        "The Godfather",
                        "https://m.media-amazon.com/images/M/MV5BM2MyNjYxNmUtYTAwNi00MTYxLWJmNWYtYzZlODY3ZTk3OTFlXkEyXkFqcGdeQXVyNzkwMjQ5NzM@._V1_SX300.jpg",
                        "1972",
                        "USA",
                        "9.2",
                        "Crime,Drama",
                        "https://example.com/images/godfather1.jpg,https://example.com/images/godfather2.jpg",
                        "2025-06-15 11:18:34",
                        0 // ✅ INT - 0 = CHƯA TẢI
                );
                movieDao.insertMovie(movie2);

                Movie movie3 = new Movie(
                        3,
                        "The Dark Knight",
                        "https://m.media-amazon.com/images/M/MV5BMTMxNTMwODM0NF5BMl5BanBnXkFtZTcwODAyMTk2Mw@@._V1_SX300.jpg",
                        "2008",
                        "USA, UK",
                        "9.0",
                        "Action,Crime,Drama,Thriller",
                        "https://example.com/images/darkknight1.jpg,https://example.com/images/darkknight2.jpg",
                        "2025-06-15 11:18:34",
                        1 // ✅ INT - 1 = ĐÃ TẢI (ví dụ)
                );
                movieDao.insertMovie(movie3);

                // Thêm chi tiết phim
                MovieDetail detail1 = new MovieDetail(
                        1,
                        "14 Oct 1994",
                        "142 min",
                        "Frank Darabont",
                        "Stephen King, Frank Darabont",
                        "Tim Robbins, Morgan Freeman, Bob Gunton",
                        "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.",
                        "Nominated for 7 Oscars. Another 21 wins & 36 nominations.",
                        "80",
                        "2,500,000",
                        "movie",
                        "https://example.com/videos/shawshank.mp4",
                        "https://example.com/subtitles/shawshank.vtt",
                        "2025-06-15 11:18:34"
                );
                movieDao.insertMovieDetail(detail1);

                MovieDetail detail2 = new MovieDetail(
                        2,
                        "24 Mar 1972",
                        "175 min",
                        "Francis Ford Coppola",
                        "Mario Puzo, Francis Ford Coppola",
                        "Marlon Brando, Al Pacino, James Caan",
                        "The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son.",
                        "Won 3 Oscars. Another 26 wins & 30 nominations.",
                        "100",
                        "1,700,000",
                        "movie",
                        "https://example.com/videos/godfather.mp4",
                        "https://example.com/subtitles/godfather.vtt",
                        "2025-06-15 11:18:34"
                );
                movieDao.insertMovieDetail(detail2);

                MovieDetail detail3 = new MovieDetail(
                        3,
                        "18 Jul 2008",
                        "152 min",
                        "Christopher Nolan",
                        "Jonathan Nolan, Christopher Nolan",
                        "Christian Bale, Heath Ledger, Aaron Eckhart",
                        "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.",
                        "Won 2 Oscars. Another 153 wins & 159 nominations.",
                        "84",
                        "2,300,000",
                        "movie",
                        "https://example.com/videos/darkknight.mp4",
                        "https://example.com/subtitles/darkknight.vtt",
                        "2025-06-15 11:18:34"
                );
                movieDao.insertMovieDetail(detail3);

                // Thêm phim yêu thích
                FavoriteMovie fav1 = new FavoriteMovie((int)adminId, 1, "2025-06-15 11:18:34");
                movieDao.insertFavoriteMovie(fav1);

                FavoriteMovie fav2 = new FavoriteMovie((int)userId, 1, "2025-06-15 11:18:34");
                movieDao.insertFavoriteMovie(fav2);

                FavoriteMovie fav3 = new FavoriteMovie((int)userId, 2, "2025-06-15 11:18:34");
                movieDao.insertFavoriteMovie(fav3);

                // ✅ SỬ DỤNG WATCHHISTORYDAO THAY VÌ MOVIEDAO CHO WATCH HISTORY
                WatchHistory hist1 = new WatchHistory((int)adminId, 1, "2025-06-14 20:30:00", 3600000); // Đã xem 1 giờ
                watchHistoryDao.insertWatchHistory(hist1);

                WatchHistory hist2 = new WatchHistory((int)userId, 3, "2025-06-13 19:15:00", 1800000); // Đã xem 30 phút
                watchHistoryDao.insertWatchHistory(hist2);

                // ✅ THÊM THÊM DỮ LIỆU LỊCH SỬ XEM MẪU
                WatchHistory hist3 = new WatchHistory((int)userId, 1, "2025-06-12 21:00:00", 5400000); // 1.5 giờ
                watchHistoryDao.insertWatchHistory(hist3);

                WatchHistory hist4 = new WatchHistory((int)adminId, 2, "2025-06-11 18:45:00", 7200000); // 2 giờ
                watchHistoryDao.insertWatchHistory(hist4);

                WatchHistory hist5 = new WatchHistory((int)userId, 2, "2025-06-10 16:20:00", 4500000); // 1.25 giờ
                watchHistoryDao.insertWatchHistory(hist5);

                // Thêm lịch sử tìm kiếm
                SearchHistory search1 = new SearchHistory((int)adminId, "drama movies", "2025-06-15 10:30:00");
                movieDao.insertSearchHistory(search1);

                SearchHistory search2 = new SearchHistory((int)userId, "action", "2025-06-15 09:15:00");
                movieDao.insertSearchHistory(search2);

                SearchHistory search3 = new SearchHistory((int)userId, "crime thriller", "2025-06-14 16:20:00");
                movieDao.insertSearchHistory(search3);

                SearchHistory search4 = new SearchHistory((int)adminId, "christopher nolan", "2025-06-13 14:10:00");
                movieDao.insertSearchHistory(search4);
            });
        }
    };
}