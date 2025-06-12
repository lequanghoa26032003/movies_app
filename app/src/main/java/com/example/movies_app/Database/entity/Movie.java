package com.example.movies_app.Database.entity;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "movies")
public class Movie {
    @PrimaryKey
    private int id;

    private String title;
    private String poster;
    private String year;
    private String country;
    private String imdbRating;
    private String genres;
    private String images;
    private String lastUpdated;
    private int isDownloaded;

    // Constructor
    public Movie(int id, String title, String poster, String year, String country,
                 String imdbRating, String genres, String images, String lastUpdated, int isDownloaded) {
        this.id = id;
        this.title = title;
        this.poster = poster;
        this.year = year;
        this.country = country;
        this.imdbRating = imdbRating;
        this.genres = genres;
        this.images = images;
        this.lastUpdated = lastUpdated;
        this.isDownloaded = isDownloaded;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getImdbRating() { return imdbRating; }
    public void setImdbRating(String imdbRating) { this.imdbRating = imdbRating; }

    public String getGenres() { return genres; }
    public void setGenres(String genres) { this.genres = genres; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }

    public int getIsDownloaded() { return isDownloaded; }
    public void setIsDownloaded(int isDownloaded) { this.isDownloaded = isDownloaded; }
}