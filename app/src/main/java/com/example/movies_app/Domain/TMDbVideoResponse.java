package com.example.movies_app.Domain;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class TMDbVideoResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("id")
    private int id;

    @SerializedName("results")
    private List<TMDbVideo> results;

    // Constructors
    public TMDbVideoResponse() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public List<TMDbVideo> getResults() { return results; }
    public void setResults(List<TMDbVideo> results) { this.results = results; }

    // Helper method to get first YouTube trailer
    public TMDbVideo getFirstYouTubeTrailer() {
        if (results != null) {
            for (TMDbVideo video : results) {
                if (video.isYouTube() && video.isTrailer()) {
                    return video;
                }
            }
        }
        return null;
    }

    // Helper method to get any YouTube video if no trailer found
    public TMDbVideo getFirstYouTubeVideo() {
        if (results != null) {
            for (TMDbVideo video : results) {
                if (video.isYouTube()) {
                    return video;
                }
            }
        }
        return null;
    }

    // Helper method to check if has any videos
    public boolean hasVideos() {
        return results != null && !results.isEmpty();
    }

    // Helper method to get official videos only
    public TMDbVideo getFirstOfficialVideo() {
        if (results != null) {
            for (TMDbVideo video : results) {
                if (video.isYouTube() && video.isOfficial()) {
                    return video;
                }
            }
        }
        return null;
    }
}