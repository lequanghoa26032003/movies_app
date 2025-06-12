package com.example.movies_app.Helper;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class FilterManager {
    private static final String PREF_NAME = "MovieFilterPrefs";
    private static final String KEY_GENRES = "selected_genres";
    private static final String KEY_YEAR_FROM = "year_from";
    private static final String KEY_YEAR_TO = "year_to";
    private static final String KEY_SORT_BY = "sort_by";

    private final SharedPreferences prefs;

    public FilterManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveGenres(Set<String> genres) {
        prefs.edit().putStringSet(KEY_GENRES, genres).apply();
    }

    public Set<String> getSelectedGenres() {
        return prefs.getStringSet(KEY_GENRES, new HashSet<>());
    }

    public void saveYearRange(int yearFrom, int yearTo) {
        prefs.edit()
                .putInt(KEY_YEAR_FROM, yearFrom)
                .putInt(KEY_YEAR_TO, yearTo)
                .apply();
    }

    public int getYearFrom() {
        return prefs.getInt(KEY_YEAR_FROM, 1970);
    }

    public int getYearTo() {
        return prefs.getInt(KEY_YEAR_TO, 2023);
    }

    public void saveSortBy(String sortBy) {
        prefs.edit().putString(KEY_SORT_BY, sortBy).apply();
    }

    public String getSortBy() {
        return prefs.getString(KEY_SORT_BY, "title");
    }

    public void resetFilters() {
        prefs.edit()
                .remove(KEY_GENRES)
                .remove(KEY_YEAR_FROM)
                .remove(KEY_YEAR_TO)
                .remove(KEY_SORT_BY)
                .apply();
    }

    public String buildFilterUrl(String baseUrl) {
        StringBuilder urlBuilder = new StringBuilder(baseUrl);

        // Thêm tham số truy vấn nếu cần
        boolean hasQueryParams = baseUrl.contains("?");

        // Thêm thể loại
        Set<String> genres = getSelectedGenres();
        if (!genres.isEmpty()) {
            if (hasQueryParams) {
                urlBuilder.append("&");
            } else {
                urlBuilder.append("?");
                hasQueryParams = true;
            }
            // Lấy thể loại đầu tiên
            urlBuilder.append("genre=").append(genres.iterator().next());
        }

        // Thêm năm
        int yearFrom = getYearFrom();
        int yearTo = getYearTo();
        if (yearFrom > 1970 || yearTo < 2023) {
            if (hasQueryParams) {
                urlBuilder.append("&");
            } else {
                urlBuilder.append("?");
                hasQueryParams = true;
            }
            urlBuilder.append("year_range=").append(yearFrom).append(",").append(yearTo);
        }

        // Thêm sắp xếp
        String sortBy = getSortBy();
        if (!sortBy.isEmpty()) {
            if (hasQueryParams) {
                urlBuilder.append("&");
            } else {
                urlBuilder.append("?");
                hasQueryParams = true;
            }
            urlBuilder.append("sort=").append(sortBy);
        }

        return urlBuilder.toString();
    }
}