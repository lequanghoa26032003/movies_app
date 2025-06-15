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

import java.util.ArrayList;
import java.util.List;

public class HistoryMoviesAdapter extends RecyclerView.Adapter<HistoryMoviesAdapter.ViewHolder> {

    public interface OnHistoryClickListener {
        void onHistoryItemClick(Movie movie);
        void onRemoveFromHistory(Movie movie);
    }

    private final Context context;
    private final List<Movie> historyList;
    private OnHistoryClickListener listener;

    public HistoryMoviesAdapter(Context context, List<Movie> historyList) {
        this.context = context;
        this.historyList = historyList != null ? historyList : new ArrayList<>();
    }

    public void setOnHistoryClickListener(OnHistoryClickListener listener) {
        this.listener = listener;
    }

    public void updateHistory(List<Movie> newList) {
        historyList.clear();
        if (newList != null) historyList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryMoviesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_history_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryMoviesAdapter.ViewHolder holder, int position) {
        Movie movie = historyList.get(position);

        // Poster
        Glide.with(context)
            .load(movie.getPoster())
            .placeholder(R.drawable.ic_movie)
            .error(R.drawable.ic_close)
            .into(holder.imgPoster);

        // Title
        holder.txtTitle.setText(movie.getTitle());

        // Watched date (use lastUpdated or custom watchTimestamp)
        String raw = movie.getLastUpdated();
        // Format if needed, or show raw
        holder.txtWatchedDate.setText(raw != null ? raw : "");

        // Click row
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onHistoryItemClick(movie);
        });

        // Remove button
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onRemoveFromHistory(movie);
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster, btnRemove;
        TextView txtTitle, txtWatchedDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster      = itemView.findViewById(R.id.imgPoster);
            txtTitle       = itemView.findViewById(R.id.txtTitle);
            txtWatchedDate = itemView.findViewById(R.id.txtWatchedDate);
            btnRemove      = itemView.findViewById(R.id.btnRemoveHistory);
        }
    }
}