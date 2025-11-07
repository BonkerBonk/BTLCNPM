package com.example.Movie_Catalog_Service.dto;


import lombok.Data;

import java.util.List;

@Data
public class MovieDto {
    private String title;
    private String description;
    private String posterUrl;
    private String backdropUrl;
    private String trailerUrl;
    private int durationMinutes;
    private List<String> genre;
    private String releaseDate; // ISO 8601 string, e.g. "2025-10-24T00:00:00Z"
    private String director;
    private List<String> cast;
}