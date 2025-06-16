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
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.movies_app.Activity.DetailActivity;
import com.example.movies_app.Database.entity.Movie;
import com.example.movies_app.R;

import java.util.List;

public class PaginatedMovieAdapter extends RecyclerView.Adapter<PaginatedMovieAdapter.ViewHolder> {
    private Context context;
    private List<Movie> movies;

    public PaginatedMovieAdapter(Context context, List<Movie> movies) {
        this.context = context;
        this.movies = movies;
    }

    public void updateMovies(List<Movie> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_paginated_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = movies.get(position);

        holder.titleTxt.setText(movie.getTitle());

        String rating = movie.getImdbRating();
        if (rating != null && !rating.isEmpty()) {
            holder.ratingTxt.setText("⭐ " + rating);
        } else {
            holder.ratingTxt.setText("⭐ N/A");
        }

        String year = movie.getYear();
        if (year != null && !year.isEmpty()) {
            holder.yearTxt.setText(year);
        } else {
            holder.yearTxt.setText("N/A");
        }

        RequestOptions requestOptions = new RequestOptions()
                .transform(new CenterCrop(), new RoundedCorners(16));

        Glide.with(context)
                .load(movie.getPoster())
                .apply(requestOptions)
                .placeholder(R.drawable.ic_refresh)
                .error(R.drawable.ic_refresh)
                .into(holder.posterImg);

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("id", movie.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movies != null ? movies.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTxt, ratingTxt, yearTxt, genreTxt;
        ImageView posterImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.movieTitle);
            ratingTxt = itemView.findViewById(R.id.movieRating);
            yearTxt = itemView.findViewById(R.id.movieYear);
            genreTxt = itemView.findViewById(R.id.movieGenre);
            posterImg = itemView.findViewById(R.id.moviePoster);
        }
    }
}