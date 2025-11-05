package com.example.Movie_Catalog_Service.model;



import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    private String movieId;
    private String title;
    private String description;
    private String posterUrl;
    private String backdropUrl;
    private String trailerUrl;
    private int durationMinutes;
    private List<String> genre;
    private Timestamp releaseDate;
    private String director;
    private List<String> cast;
}