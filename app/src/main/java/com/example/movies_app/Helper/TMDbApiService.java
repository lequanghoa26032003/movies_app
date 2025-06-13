package com.example.movies_app.Helper;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.movies_app.Domain.TMDbSearchResponse;
import com.example.movies_app.Domain.TMDbVideoResponse;
import com.google.gson.Gson;

public class TMDbApiService {
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final String API_KEY = "aa727220bc566847094471483e61204e";

    private RequestQueue requestQueue;
    private Gson gson;

    public TMDbApiService(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        gson = new Gson();
    }

    // Get popular movies
    public void getPopularMovies(int page, SearchCallback callback) {
        String url = BASE_URL + "/movie/popular?api_key=" + API_KEY +
                "&page=" + page + "&language=vi";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        TMDbSearchResponse searchResponse = gson.fromJson(response, TMDbSearchResponse.class);
                        callback.onSuccess(searchResponse);
                    } catch (Exception e) {
                        callback.onError("Lỗi parse dữ liệu: " + e.getMessage());
                    }
                },
                error -> callback.onError("Lỗi kết nối: " + error.getMessage()));

        requestQueue.add(request);
    }

    // Search movies
    public void searchMovies(String query, int page, SearchCallback callback) {
        String url = BASE_URL + "/search/movie?api_key=" + API_KEY +
                "&query=" + query + "&page=" + page + "&language=vi";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        TMDbSearchResponse searchResponse = gson.fromJson(response, TMDbSearchResponse.class);
                        callback.onSuccess(searchResponse);
                    } catch (Exception e) {
                        callback.onError("Lỗi parse dữ liệu: " + e.getMessage());
                    }
                },
                error -> callback.onError("Lỗi kết nối: " + error.getMessage()));

        requestQueue.add(request);
    }

    // Get movie videos - này mới có key video
    public void getMovieVideos(int movieId, VideoCallback callback) {
        String url = BASE_URL + "/movie/" + movieId + "/videos?api_key=" + API_KEY;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        TMDbVideoResponse videoResponse = gson.fromJson(response, TMDbVideoResponse.class);
                        callback.onSuccess(videoResponse);
                    } catch (Exception e) {
                        callback.onError("Lỗi parse video data: " + e.getMessage());
                    }
                },
                error -> callback.onError("Lỗi kết nối video API: " + error.getMessage()));

        requestQueue.add(request);
    }

    // Callback interfaces
    public interface SearchCallback {
        void onSuccess(TMDbSearchResponse response);
        void onError(String error);
    }

    public interface VideoCallback {
        void onSuccess(TMDbVideoResponse response);
        void onError(String error);
    }
}