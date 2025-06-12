package com.example.movies_app.Database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "user_preferences",
        primaryKeys = {"userId"},  // userId là khóa chính
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "userId",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("userId")}
)
public class UserPreference {
    private int userId;  // Khóa chính và là khóa ngoại liên kết với User

    private boolean darkMode;

    private boolean notificationsEnabled;

    private String preferredLanguage;

    private String favoriteGenres;

    private int downloadQuality;

    private boolean autoPlayNextEpisode;

    // Constructor
    public UserPreference(int userId) {
        this.userId = userId;
        this.darkMode = false;
        this.notificationsEnabled = true;
        this.preferredLanguage = "vi";
        this.favoriteGenres = "[]";
        this.downloadQuality = 1;
        this.autoPlayNextEpisode = true;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public boolean isDarkMode() { return darkMode; }
    public void setDarkMode(boolean darkMode) { this.darkMode = darkMode; }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }

    public String getFavoriteGenres() { return favoriteGenres; }
    public void setFavoriteGenres(String favoriteGenres) { this.favoriteGenres = favoriteGenres; }

    public int getDownloadQuality() { return downloadQuality; }
    public void setDownloadQuality(int downloadQuality) { this.downloadQuality = downloadQuality; }

    public boolean isAutoPlayNextEpisode() { return autoPlayNextEpisode; }
    public void setAutoPlayNextEpisode(boolean autoPlayNextEpisode) { this.autoPlayNextEpisode = autoPlayNextEpisode; }
}