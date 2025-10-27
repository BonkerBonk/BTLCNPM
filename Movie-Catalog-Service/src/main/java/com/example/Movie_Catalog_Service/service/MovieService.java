package com.example.Movie_Catalog_Service.service;



import com.example.Movie_Catalog_Service.dto.MovieDto;
import com.example.Movie_Catalog_Service.model.Movie;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface MovieService {
    Movie createMovie(MovieDto dto) throws ExecutionException, InterruptedException;
    List<Movie> getAllMovies() throws ExecutionException, InterruptedException;
    Movie getMovieById(String id) throws ExecutionException, InterruptedException;
    void deleteMovie(String id);
}
