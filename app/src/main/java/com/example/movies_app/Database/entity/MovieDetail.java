package com.example.movies_app.Database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "movie_details",
        foreignKeys = @ForeignKey(
                entity = Movie.class,
                parentColumns = "id",
                childColumns = "movieId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("movieId")}
)
public class MovieDetail implements Serializable {
    @PrimaryKey
    private int movieId;  // Vừa là khóa chính vừa là khóa ngoại đến Movie

    private String released;
    private String runtime;
    private String director;
    private String writer;
    private String actors;
    private String plot;
    private String awards;
    private String metascore;
    private String imdbVotes;
    private String type;
    private String videoUrl;
    private String subtitleUrl;
    private String lastUpdated;

    @Ignore
    public MovieDetail(int movieId, String released, String runtime, String director,
                       String writer, String actors, String plot, String awards,
                       String metascore, String imdbVotes, String type,
                       String videoUrl, String subtitleUrl, String lastUpdated) {
        this.movieId = movieId;
        this.released = released;
        this.runtime = runtime;
        this.director = director;
        this.writer = writer;
        this.actors = actors;
        this.plot = plot;
        this.awards = awards;
        this.metascore = metascore;
        this.imdbVotes = imdbVotes;
        this.type = type;
        this.videoUrl = videoUrl;
        this.subtitleUrl = subtitleUrl;
        this.lastUpdated = lastUpdated;
    }

    // Constructor mặc định (cần cho Room và để tạo instance mới)
    public MovieDetail() {}

    // Getters and Setters
    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    // Thêm phương thức getId() và setId() để tương thích với code cũ
    public int getId() { return movieId; }
    public void setId(int id) { this.movieId = id; }

    public String getReleased() { return released; }
    public void setReleased(String released) { this.released = released; }

    public String getRuntime() { return runtime; }
    public void setRuntime(String runtime) { this.runtime = runtime; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public String getWriter() { return writer; }
    public void setWriter(String writer) { this.writer = writer; }

    public String getActors() { return actors; }
    public void setActors(String actors) { this.actors = actors; }

    public String getPlot() { return plot; }
    public void setPlot(String plot) { this.plot = plot; }

    public String getAwards() { return awards; }
    public void setAwards(String awards) { this.awards = awards; }

    public String getMetascore() { return metascore; }
    public void setMetascore(String metascore) { this.metascore = metascore; }

    public String getImdbVotes() { return imdbVotes; }
    public void setImdbVotes(String imdbVotes) { this.imdbVotes = imdbVotes; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getSubtitleUrl() { return subtitleUrl; }
    public void setSubtitleUrl(String subtitleUrl) { this.subtitleUrl = subtitleUrl; }

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }

    // Thêm phương thức getRated() để tương thích với code cũ
    public String getRated() { return type; } // Giả sử 'type' là rated info
    public void setRated(String rated) { this.type = rated; }
}