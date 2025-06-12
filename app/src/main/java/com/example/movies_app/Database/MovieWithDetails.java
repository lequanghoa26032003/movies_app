package com.example.movies_app.Database;


import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.movies_app.Database.entity.Movie;
import com.example.movies_app.Database.entity.MovieDetail;

public class MovieWithDetails {
    @Embedded
    public Movie movie;

    @Relation(
            parentColumn = "id",
            entityColumn = "movieId"
    )
    public MovieDetail details;
}