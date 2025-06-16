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
import com.example.movies_app.Database.entity.Movie;
import com.example.movies_app.R;
import java.util.List;
import java.util.Locale;

public class Top10MoviesAdapter extends RecyclerView.Adapter<Top10MoviesAdapter.ViewHolder> {

    private List<Movie> movies;
    private Context context;
    private OnMovieClickListener listener;

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    public Top10MoviesAdapter(Context context, List<Movie> movies) {
        this.context = context;
        this.movies = movies;
    }

    public void setOnMovieClickListener(OnMovieClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.top_movie_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = movies.get(position);

        // Rank number
        if (holder.rankNumber != null) {
            holder.rankNumber.setText(String.valueOf(position + 1));
            setRankBackground(holder.rankNumber, position);
        }

        // Movie title
        if (holder.movieTitle != null) {
            holder.movieTitle.setText(movie.getTitle() != null ? movie.getTitle() : "Không có tiêu đề");
        }

        // Movie genre
        if (holder.movieGenre != null) {
            holder.movieGenre.setText(movie.getGenres() != null ? movie.getGenres() : "Chưa xác định");
        }

        // View count
        if (holder.viewCount != null) {
            holder.viewCount.setText(formatViewCount(movie.getViewCount()));
        }

        // Movie poster
        if (holder.moviePoster != null) {
            if (movie.getPoster() != null && !movie.getPoster().isEmpty()) {
                Glide.with(context)
                        .load(movie.getPoster())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(holder.moviePoster);
            } else {
                holder.moviePoster.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMovieClick(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies != null ? movies.size() : 0;
    }

    private String formatViewCount(int viewCount) {
        if (viewCount >= 1000000) {
            return String.format(Locale.getDefault(), "%.1fM", viewCount / 1000000.0);
        } else if (viewCount >= 1000) {
            return String.format(Locale.getDefault(), "%.1fK", viewCount / 1000.0);
        } else {
            return String.valueOf(viewCount);
        }
    }

    private void setRankBackground(TextView rankView, int position) {
        switch (position) {
            case 0: // 1st place - Gold
                rankView.setBackgroundColor(0xFFFFD700); // Gold
                rankView.setTextColor(0xFF000000); // Black text
                break;
            case 1: // 2nd place - Silver
                rankView.setBackgroundColor(0xFFC0C0C0); // Silver
                rankView.setTextColor(0xFF000000); // Black text
                break;
            case 2: // 3rd place - Bronze
                rankView.setBackgroundColor(0xFFCD7F32); // Bronze
                rankView.setTextColor(0xFFFFFFFF); // White text
                break;
            default:
                rankView.setBackgroundColor(0xFF666666); // Dark gray
                rankView.setTextColor(0xFFFFFFFF); // White text
                break;
        }
    }

    public void updateData(List<Movie> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rankNumber, movieTitle, movieGenre, viewCount;
        ImageView moviePoster;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Safely find views with null checks
            try {
                rankNumber = itemView.findViewById(R.id.rankNumber);
            } catch (Exception e) {
                rankNumber = null;
            }

            try {
                movieTitle = itemView.findViewById(R.id.movieTitle);
            } catch (Exception e) {
                movieTitle = null;
            }

            try {
                movieGenre = itemView.findViewById(R.id.movieGenre);
            } catch (Exception e) {
                movieGenre = null;
            }

            try {
                viewCount = itemView.findViewById(R.id.viewCount);
            } catch (Exception e) {
                viewCount = null;
            }

            try {
                moviePoster = itemView.findViewById(R.id.moviePoster);
            } catch (Exception e) {
                moviePoster = null;
            }
        }
    }
}