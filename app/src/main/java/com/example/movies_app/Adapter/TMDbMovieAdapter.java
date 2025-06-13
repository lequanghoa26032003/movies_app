package com.example.movies_app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.movies_app.Domain.TMDbMovie;
import com.example.movies_app.R;
import java.util.List;

public class TMDbMovieAdapter extends RecyclerView.Adapter<TMDbMovieAdapter.MovieViewHolder> {
    private List<TMDbMovie> movies;
    private Context context;
    private OnMovieClickListener listener;

    public interface OnMovieClickListener {
        void onMovieClick(TMDbMovie movie);
        void onAddMovieClick(TMDbMovie movie);
    }

    public TMDbMovieAdapter(Context context, List<TMDbMovie> movies) {
        this.context = context;
        this.movies = movies;
    }

    public void setOnMovieClickListener(OnMovieClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tmdb_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        TMDbMovie movie = movies.get(position);

        holder.titleText.setText(movie.getTitle());
        holder.overviewText.setText(movie.getOverview());
        holder.releaseDateText.setText("Phát hành: " + movie.getReleaseDate());
        holder.ratingText.setText(String.format("⭐ %.1f/10", movie.getVoteAverage()));

        // Load poster image
        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            Glide.with(context)
                    .load(movie.getFullPosterUrl())
                    .placeholder(R.drawable.ic_movie)
                    .error(R.drawable.ic_movie)
                    .into(holder.posterImage);
        } else {
            holder.posterImage.setImageResource(R.drawable.ic_movie);
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMovieClick(movie);
            }
        });

        holder.addButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddMovieClick(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies != null ? movies.size() : 0;
    }

    public void updateMovies(List<TMDbMovie> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImage;
        TextView titleText, overviewText, releaseDateText, ratingText;
        View addButton;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImage = itemView.findViewById(R.id.moviePoster);
            titleText = itemView.findViewById(R.id.movieTitle);
            overviewText = itemView.findViewById(R.id.movieOverview);
            releaseDateText = itemView.findViewById(R.id.movieReleaseDate);
            ratingText = itemView.findViewById(R.id.movieRating);
            addButton = itemView.findViewById(R.id.addMovieButton);
        }
    }
}