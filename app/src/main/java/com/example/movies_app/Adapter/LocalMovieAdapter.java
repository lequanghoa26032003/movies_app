package com.example.movies_app.Adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.movies_app.Database.entity.Movie;
import com.example.movies_app.R;

import java.util.List;

public class LocalMovieAdapter extends RecyclerView.Adapter<LocalMovieAdapter.LocalMovieViewHolder> {

    private Context context;
    private List<Movie> moviesList;
    private OnLocalMovieClickListener listener;

    public interface OnLocalMovieClickListener {
        void onLocalMovieClick(Movie movie);
        void onEditMovieClick(Movie movie);
        void onDeleteMovieClick(Movie movie);
    }

    public LocalMovieAdapter(Context context, List<Movie> moviesList) {
        this.context = context;
        this.moviesList = moviesList;
    }

    public void setOnLocalMovieClickListener(OnLocalMovieClickListener listener) {
        this.listener = listener;
    }

    public void updateMovies(List<Movie> newMovies) {
        this.moviesList = newMovies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LocalMovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_local_movie, parent, false);
        return new LocalMovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocalMovieViewHolder holder, int position) {
        if (moviesList != null && position < moviesList.size()) {
            Movie movie = moviesList.get(position);
            holder.bind(movie);
        }
    }

    @Override
    public int getItemCount() {
        // FIX: Kiểm tra null trước khi gọi size()
        return moviesList != null ? moviesList.size() : 0;
    }

    class LocalMovieViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgPoster;
        private TextView txtTitle, txtYear, txtGenres, txtRating, txtCountry;
        private Button btnEdit, btnDelete, btnView;

        public LocalMovieViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPoster = itemView.findViewById(R.id.imgPoster);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtYear = itemView.findViewById(R.id.txtYear);
            txtGenres = itemView.findViewById(R.id.txtGenres);
            txtRating = itemView.findViewById(R.id.txtRating);
            txtCountry = itemView.findViewById(R.id.txtCountry);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnView = itemView.findViewById(R.id.btnView);
        }

        public void bind(Movie movie) {
            // Kiểm tra null trước khi bind
            if (movie == null) return;

            txtTitle.setText(movie.getTitle() != null ? movie.getTitle() : "Unknown");
            txtYear.setText(movie.getYear() != null ? movie.getYear() : "Unknown");
            txtGenres.setText(movie.getGenres() != null ? movie.getGenres() : "Unknown");
            txtRating.setText("IMDb: " + (movie.getImdbRating() != null ? movie.getImdbRating() : "N/A"));
            txtCountry.setText(movie.getCountry() != null ? movie.getCountry() : "Unknown");

            // Load poster image
            if (!TextUtils.isEmpty(movie.getPoster())) {
                Glide.with(context)
                        .load(movie.getPoster())
                        .placeholder(R.drawable.placeholder_movie)
                        .error(R.drawable.placeholder_movie)
                        .into(imgPoster);
            } else {
                imgPoster.setImageResource(R.drawable.placeholder_movie);
            }

            // Set click listeners
            btnView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLocalMovieClick(movie);
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditMovieClick(movie);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteMovieClick(movie);
                }
            });

            // Click on poster/title to view details
            View.OnClickListener viewDetailsListener = v -> {
                if (listener != null) {
                    listener.onLocalMovieClick(movie);
                }
            };

            imgPoster.setOnClickListener(viewDetailsListener);
            txtTitle.setOnClickListener(viewDetailsListener);
        }
    }
}