package com.example.movies_app.Domain;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class TMDbMovie implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("overview")
    private String overview;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("backdrop_path")
    private String backdropPath;

    @SerializedName("release_date")
    private String releaseDate;

    @SerializedName("vote_average")
    private double voteAverage;

    @SerializedName("vote_count")
    private int voteCount;

    @SerializedName("genre_ids")
    private List<Integer> genreIds;

    @SerializedName("adult")
    private boolean adult;

    @SerializedName("original_language")
    private String originalLanguage;

    @SerializedName("popularity")
    private double popularity;

    // Thêm các trường mở rộng
    private String videoUrl;
    private String youtubeKey; // YouTube video key
    private String director;
    private String actors;
    private String writer;
    private String country;
    private String awards;
    private String runtime;
    private String rated;
    private String imdbId;
    private String genresText;
    private boolean isAddedToSystem = false;
    private int localId = -1;

    // Constructors
    public TMDbMovie() {}

    // Existing getters and setters...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public String getBackdropPath() { return backdropPath; }
    public void setBackdropPath(String backdropPath) { this.backdropPath = backdropPath; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(double voteAverage) { this.voteAverage = voteAverage; }

    public int getVoteCount() { return voteCount; }
    public void setVoteCount(int voteCount) { this.voteCount = voteCount; }

    public List<Integer> getGenreIds() { return genreIds; }
    public void setGenreIds(List<Integer> genreIds) { this.genreIds = genreIds; }

    public boolean isAdult() { return adult; }
    public void setAdult(boolean adult) { this.adult = adult; }

    public String getOriginalLanguage() { return originalLanguage; }
    public void setOriginalLanguage(String originalLanguage) { this.originalLanguage = originalLanguage; }

    public double getPopularity() { return popularity; }
    public void setPopularity(double popularity) { this.popularity = popularity; }

    // New getters and setters for extended fields
    public String getVideoUrl() {
        if (videoUrl != null && !videoUrl.isEmpty()) {
            return videoUrl;
        }
        // Nếu có YouTube key, tạo URL YouTube
        if (youtubeKey != null && !youtubeKey.isEmpty()) {
            return "https://www.youtube.com/watch?v=" + youtubeKey;
        }
        return null;
    }

    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getYoutubeKey() { return youtubeKey; }
    public void setYoutubeKey(String youtubeKey) {
        this.youtubeKey = youtubeKey;
        // Tự động tạo video URL từ YouTube key
        if (youtubeKey != null && !youtubeKey.isEmpty()) {
            this.videoUrl = "https://www.youtube.com/watch?v=" + youtubeKey;
        }
    }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public String getActors() { return actors; }
    public void setActors(String actors) { this.actors = actors; }

    public String getWriter() { return writer; }
    public void setWriter(String writer) { this.writer = writer; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getAwards() { return awards; }
    public void setAwards(String awards) { this.awards = awards; }

    public String getRuntime() { return runtime; }
    public void setRuntime(String runtime) { this.runtime = runtime; }

    public String getRated() { return rated; }
    public void setRated(String rated) { this.rated = rated; }

    public String getImdbId() { return imdbId; }
    public void setImdbId(String imdbId) { this.imdbId = imdbId; }

    public String getGenresText() { return genresText; }
    public void setGenresText(String genresText) { this.genresText = genresText; }

    public boolean isAddedToSystem() { return isAddedToSystem; }
    public void setAddedToSystem(boolean addedToSystem) { isAddedToSystem = addedToSystem; }

    public int getLocalId() { return localId; }
    public void setLocalId(int localId) { this.localId = localId; }

    // Helper methods
    public String getFullPosterUrl() {
        return posterPath != null ? "https://image.tmdb.org/t/p/w500" + posterPath : "";
    }

    public String getFullBackdropUrl() {
        return backdropPath != null ? "https://image.tmdb.org/t/p/w780" + backdropPath : "";
    }

    public String getYearFromReleaseDate() {
        if (releaseDate != null && releaseDate.length() >= 4) {
            return releaseDate.substring(0, 4);
        }
        return "";
    }

    // Method to get YouTube embed URL for web view
    public String getYoutubeEmbedUrl() {
        if (youtubeKey != null && !youtubeKey.isEmpty()) {
            return "https://www.youtube.com/embed/" + youtubeKey;
        }
        return null;
    }

    // Method to check if movie has complete information for detail view
    public boolean hasCompleteInfo() {
        return (videoUrl != null && !videoUrl.isEmpty()) || (youtubeKey != null && !youtubeKey.isEmpty());
    }
    public String getPlayableVideoUrl() {
        if (youtubeKey != null && !youtubeKey.isEmpty()) {
            return "https://www.youtube.com/embed/" + youtubeKey;
        }
        return videoUrl;
    }

    public boolean hasVideo() {
        return (youtubeKey != null && !youtubeKey.isEmpty()) ||
                (videoUrl != null && !videoUrl.isEmpty());
    }
}