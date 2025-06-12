package com.example.movies_app.Database.entity;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "search_history",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "userId",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("userId")}
)
public class SearchHistory {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int userId;
    private String query;
    private String searchDate;

    public SearchHistory(int userId, String query, String searchDate) {
        this.userId = userId;
        this.query = query;
        this.searchDate = searchDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getSearchDate() { return searchDate; }
    public void setSearchDate(String searchDate) { this.searchDate = searchDate; }
}