package com.example.movies_app.Domain;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TMDbSearchResponse {
    @SerializedName("page")
    private int page;

    @SerializedName("results")
    private List<TMDbMovie> results;

    @SerializedName("total_pages")
    private int totalPages;

    @SerializedName("total_results")
    private int totalResults;

    // Constructors
    public TMDbSearchResponse() {}

    // Getters and Setters
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public List<TMDbMovie> getResults() { return results; }
    public void setResults(List<TMDbMovie> results) { this.results = results; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public int getTotalResults() { return totalResults; }
    public void setTotalResults(int totalResults) { this.totalResults = totalResults; }
}