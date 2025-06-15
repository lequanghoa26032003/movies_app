package com.example.movies_app.Database.entity;

import androidx.room.Embedded;

/**
 * DTO dùng để Room ánh xạ kết quả JOIN giữa watch_history và movies.
 */
public class HistoryEntry {
    @Embedded
    public Movie movie;      // tất cả cột của Movie

    public String watchDate; // từ watch_history.watchDate
}