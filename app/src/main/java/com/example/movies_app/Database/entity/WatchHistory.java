package com.example.movies_app.Database.entity;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "watch_history",
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
public class WatchHistory {
    @PrimaryKey(autoGenerate = true)
    private int historyId;

    private int userId;
    private int movieId;
    private String watchDate;
    private long position;

    public WatchHistory(int userId, int movieId, String watchDate, long position) {
        this.userId = userId;
        this.movieId = movieId;
        this.watchDate = watchDate;
        this.position = position;
    }

    public int getHistoryId() { return historyId; }
    public void setHistoryId(int historyId) { this.historyId = historyId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public String getWatchDate() { return watchDate; }
    public void setWatchDate(String watchDate) { this.watchDate = watchDate; }

    public long getPosition() { return position; }
    public void setPosition(long position) { this.position = position; }
}