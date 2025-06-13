package com.example.movies_app.Helper;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.movies_app.Domain.TMDbSearchResponse;
import com.google.gson.Gson;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class TMDbApiService {
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final String API_KEY = "aa727220bc566847094471483e61204e";
    private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJhYTcyNzIyMGJjNTY2ODQ3MDk0NDcxNDgzZTYxMjA0ZSIsIm5iZiI6MTc0ODQ4NjI4MC41MDIsInN1YiI6IjY4MzdjODg4YmZmNmY1Y2RkOWViNDJiZCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.giFWO3l0BS72zvVRZdnLES51df11T_FH9pSo0deDwyc";

    private RequestQueue requestQueue;
    private Context context;

    public TMDbApiService(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    // Interface cho callback
    public interface SearchCallback {
        void onSuccess(TMDbSearchResponse response);
        void onError(String error);
    }

    public interface MovieDetailCallback {
        void onSuccess(String movieDetail);
        void onError(String error);
    }

    /**
     * Tìm kiếm phim theo tên
     */
    public void searchMovies(String query, int page, SearchCallback callback) {
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String url = BASE_URL + "/search/movie?api_key=" + API_KEY +
                    "&query=" + encodedQuery +
                    "&page=" + page +
                    "&language=vi-VN";

            StringRequest request = new StringRequest(Request.Method.GET, url,
                    response -> {
                        try {
                            Gson gson = new Gson();
                            TMDbSearchResponse searchResponse = gson.fromJson(response, TMDbSearchResponse.class);
                            callback.onSuccess(searchResponse);
                        } catch (Exception e) {
                            callback.onError("Lỗi parse dữ liệu: " + e.getMessage());
                        }
                    },
                    error -> callback.onError("Lỗi kết nối: " + error.getMessage())
            );

            requestQueue.add(request);

        } catch (UnsupportedEncodingException e) {
            callback.onError("Lỗi encoding: " + e.getMessage());
        }
    }

    /**
     * Lấy chi tiết phim theo ID
     */
    public void getMovieDetails(int movieId, MovieDetailCallback callback) {
        String url = BASE_URL + "/movie/" + movieId + "?api_key=" + API_KEY + "&language=vi-VN";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> callback.onSuccess(response),
                error -> callback.onError("Lỗi khi lấy chi tiết phim: " + error.getMessage())
        );

        requestQueue.add(request);
    }

    /**
     * Lấy danh sách thể loại
     */
    public void getGenres(MovieDetailCallback callback) {
        String url = BASE_URL + "/genre/movie/list?api_key=" + API_KEY + "&language=vi-VN";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> callback.onSuccess(response),
                error -> callback.onError("Lỗi khi lấy danh sách thể loại: " + error.getMessage())
        );

        requestQueue.add(request);
    }
}