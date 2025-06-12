package com.example.movies_app.Database.entity;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "favorite_movies",
        primaryKeys = {"userId", "movieId"},  // Khóa chính kết hợp
        foreignKeys = {
                @ForeignKey(
                        entity = Movie.class,
                        parentColumns = "id",
                        childColumns = "movieId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "userId",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("movieId"),
                @Index("userId")
        }
)
public class FavoriteMovie {
    private int userId;  // Một phần của khóa chính kết hợp
    private int movieId; // Một phần của khóa chính kết hợp
    private String dateAdded;

    public FavoriteMovie(int userId, int movieId, String dateAdded) {
        this.userId = userId;
        this.movieId = movieId;
        this.dateAdded = dateAdded;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public String getDateAdded() { return dateAdded; }
    public void setDateAdded(String dateAdded) { this.dateAdded = dateAdded; }
}