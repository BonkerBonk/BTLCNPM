package com.example.Movie_Catalog_Service.service.impl;

import com.example.Movie_Catalog_Service.dto.MovieDto;
import com.example.Movie_Catalog_Service.model.Movie;
import com.example.Movie_Catalog_Service.repository.MovieRepository;
import com.example.Movie_Catalog_Service.service.MovieService;
import com.google.cloud.Timestamp;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    public MovieServiceImpl(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    public Movie createMovie(MovieDto dto) throws ExecutionException, InterruptedException {
        Movie movie = new Movie();
        movie.setTitle(dto.getTitle());
        movie.setDescription(dto.getDescription());
        movie.setPosterUrl(dto.getPosterUrl());
        movie.setBackdropUrl(dto.getBackdropUrl());
        movie.setTrailerUrl(dto.getTrailerUrl());
        movie.setDurationMinutes(dto.getDurationMinutes());
        movie.setGenre(dto.getGenre());
        movie.setReleaseDate(Timestamp.parseTimestamp(dto.getReleaseDate()));
        movie.setDirector(dto.getDirector());
        movie.setCast(dto.getCast());
        return movieRepository.save(movie);
    }

    @Override
    public List<Movie> getAllMovies() throws ExecutionException, InterruptedException {
        return movieRepository.getAll();
    }

    @Override
    public Movie getMovieById(String id) throws ExecutionException, InterruptedException {
        return movieRepository.getById(id);
    }

    @Override
    public void deleteMovie(String id) {
        movieRepository.deleteById(id);
    }
}
