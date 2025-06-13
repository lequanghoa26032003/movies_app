package com.example.movies_app.Domain;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class TMDbVideo implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("id")
    private String id;

    @SerializedName("key")
    private String key;

    @SerializedName("name")
    private String name;

    @SerializedName("site")
    private String site;

    @SerializedName("type")
    private String type;

    @SerializedName("official")
    private boolean official;

    @SerializedName("published_at")
    private String publishedAt;

    // Constructors
    public TMDbVideo() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSite() { return site; }
    public void setSite(String site) { this.site = site; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isOfficial() { return official; }
    public void setOfficial(boolean official) { this.official = official; }

    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }

    // Helper methods
    public boolean isYouTube() {
        return "YouTube".equalsIgnoreCase(site);
    }

    public boolean isTrailer() {
        return "Trailer".equalsIgnoreCase(type);
    }

    public String getYouTubeUrl() {
        if (isYouTube() && key != null && !key.isEmpty()) {
            return "https://www.youtube.com/watch?v=" + key;
        }
        return null;
    }

    public String getYouTubeEmbedUrl() {
        if (isYouTube() && key != null && !key.isEmpty()) {
            return "https://www.youtube.com/embed/" + key;
        }
        return null;
    }
}