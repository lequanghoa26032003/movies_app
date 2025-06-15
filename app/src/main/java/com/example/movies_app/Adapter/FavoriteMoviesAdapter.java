package com.example.movies_app.Adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.movies_app.Database.entity.Movie;
import com.example.movies_app.R;

import java.util.List;

public class FavoriteMoviesAdapter extends RecyclerView.Adapter<FavoriteMoviesAdapter.FavoriteMovieViewHolder> {

    private Context context;
    private List<Movie> favoriteMovies;
    private OnFavoriteMovieClickListener listener;

    public interface OnFavoriteMovieClickListener {
        void onFavoriteMovieClick(Movie movie);
        void onRemoveFromFavorites(Movie movie);
    }

    public FavoriteMoviesAdapter(Context context, List<Movie> favoriteMovies) {
        this.context = context;
        this.favoriteMovies = favoriteMovies;
    }

    public void setOnFavoriteMovieClickListener(OnFavoriteMovieClickListener listener) {
        this.listener = listener;
    }

    public void updateFavoriteMovies(List<Movie> newFavoriteMovies) {
        this.favoriteMovies = newFavoriteMovies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavoriteMovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_movie, parent, false);
        return new FavoriteMovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteMovieViewHolder holder, int position) {
        if (favoriteMovies != null && position < favoriteMovies.size()) {
            Movie movie = favoriteMovies.get(position);
            holder.bind(movie);
        }
    }

    @Override
    public int getItemCount() {
        return favoriteMovies != null ? favoriteMovies.size() : 0;
    }

    class FavoriteMovieViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgPoster, btnRemoveFavorite;
        private TextView txtTitle, txtYear, txtRating;

        public FavoriteMovieViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPoster = itemView.findViewById(R.id.imgPoster);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtYear = itemView.findViewById(R.id.txtYear);
            txtRating = itemView.findViewById(R.id.txtRating);
            btnRemoveFavorite = itemView.findViewById(R.id.btnRemoveFavorite);
        }

        public void bind(Movie movie) {
            if (movie == null) return;

            txtTitle.setText(movie.getTitle() != null ? movie.getTitle() : "Unknown");
            txtYear.setText(movie.getYear() != null ? movie.getYear() : "Unknown");
            txtRating.setText("IMDb: " + (movie.getImdbRating() != null ? movie.getImdbRating() : "N/A"));

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
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteMovieClick(movie);
                }
            });

            btnRemoveFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveFromFavorites(movie);
                }
            });
        }
    }
}