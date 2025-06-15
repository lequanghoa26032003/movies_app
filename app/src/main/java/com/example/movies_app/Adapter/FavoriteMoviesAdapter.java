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
import com.example.movies_app.R;
import com.example.movies_app.Database.entity.Movie;

import java.util.ArrayList;
import java.util.List;

public class FavoriteMoviesAdapter extends RecyclerView.Adapter<FavoriteMoviesAdapter.ViewHolder> {

    public interface OnFavoriteMovieClickListener {
        void onFavoriteMovieClick(Movie movie);
        void onRemoveFromFavorites(Movie movie);
    }

    private final Context context;
    private final List<Movie> favoriteList;
    private OnFavoriteMovieClickListener listener;

    public FavoriteMoviesAdapter(Context context, List<Movie> favoriteList) {
        this.context = context;
        this.favoriteList = favoriteList != null ? favoriteList : new ArrayList<>();
    }

    public void setOnFavoriteMovieClickListener(OnFavoriteMovieClickListener listener) {
        this.listener = listener;
    }

    /**
     * Cập nhật danh sách phim mới và refresh RecyclerView
     */
    public void updateFavoriteMovies(List<Movie> newList) {
        favoriteList.clear();
        if (newList != null) {
            favoriteList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavoriteMoviesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_favorite_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteMoviesAdapter.ViewHolder holder, int position) {
        Movie movie = favoriteList.get(position);

        // Load poster URL from Movie.poster
        Glide.with(context)
                .load(movie.getPoster())
                .placeholder(R.drawable.ic_movie)      // dùng ic_movie làm placeholder
                .error(R.drawable.ic_close)           // dùng ic_close làm icon lỗi
                .into(holder.imgPoster);

        // Tiêu đề
        holder.txtTitle.setText(movie.getTitle());

        // Năm phát hành
        holder.txtYear.setText(movie.getYear() != null ? movie.getYear() : "");

        // Xếp hạng (IMDB rating)
        holder.txtRating.setText(movie.getImdbRating() != null ? movie.getImdbRating() : "-");

        // Click vào item để xem chi tiết
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteMovieClick(movie);
            }
        });

        // Click vào nút remove để xóa khỏi yêu thích
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveFromFavorites(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        ImageView btnRemove;
        TextView txtTitle, txtYear, txtRating;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster  = itemView.findViewById(R.id.imgPoster);
            btnRemove  = itemView.findViewById(R.id.btnRemoveFavorite);
            txtTitle   = itemView.findViewById(R.id.txtTitle);
            txtYear    = itemView.findViewById(R.id.txtYear);
            txtRating  = itemView.findViewById(R.id.txtRating);
        }
    }
}