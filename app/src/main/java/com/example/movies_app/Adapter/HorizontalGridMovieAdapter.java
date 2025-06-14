package com.example.movies_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.movies_app.Activity.DetailActivity;
import com.example.movies_app.Database.entity.Movie;
import com.example.movies_app.R;

import java.util.List;

public class HorizontalGridMovieAdapter extends RecyclerView.Adapter<HorizontalGridMovieAdapter.ViewHolder> {
    private List<Movie> movies;
    private Context context;

    public HorizontalGridMovieAdapter(Context context, List<Movie> movies) {
        this.context = context;
        this.movies = movies;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout giống như FilmListAdapter
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_film, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = movies.get(position);

        // Set title và rating
        holder.titleTxt.setText(movie.getTitle() != null ? movie.getTitle() : "Unknown");
        holder.scoreTxt.setText(movie.getImdbRating() != null ? movie.getImdbRating() : "N/A");

        // Load poster
        if (movie.getPoster() != null && !movie.getPoster().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(movie.getPoster())
                    .placeholder(R.drawable.placeholder_movie)
                    .error(R.drawable.placeholder_movie)
                    .into(holder.pic);
        } else {
            holder.pic.setImageResource(R.drawable.placeholder_movie);
        }

        // Set click listener - TRUYỀN ĐÚNG ID
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), DetailActivity.class);
            intent.putExtra("id", movie.getId()); // Truyền ID từ database
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movies != null ? movies.size() : 0;
    }

    public void updateMovies(List<Movie> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTxt, scoreTxt;
        ImageView pic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.titleTxt);
            scoreTxt = itemView.findViewById(R.id.scoreTxt);
            pic = itemView.findViewById(R.id.pic);
        }
    }
}