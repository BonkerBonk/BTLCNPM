package com.example.Movie_Catalog_Service.service.impl;

import com.example.Movie_Catalog_Service.dto.MovieDto;
import com.example.Movie_Catalog_Service.model.Movie;
import com.example.Movie_Catalog_Service.repository.MovieRepository;
import com.example.Movie_Catalog_Service.service.MovieService;
import com.google.cloud.Timestamp; // Make sure Timestamp is imported
import org.springframework.stereotype.Service;

// import java.time.Instant; // Not directly needed if using Timestamp.now()
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors; // Import Collectors for filtering

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    public MovieServiceImpl(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    public Movie createMovie(MovieDto dto) throws ExecutionException, InterruptedException {
        Movie movie = new Movie();
        // Generate ID or let repository handle it if necessary
        // movie.setMovieId(...);
        movie.setTitle(dto.getTitle());
        movie.setDescription(dto.getDescription());
        movie.setPosterUrl(dto.getPosterUrl());
        movie.setBackdropUrl(dto.getBackdropUrl());
        movie.setTrailerUrl(dto.getTrailerUrl());
        movie.setDurationMinutes(dto.getDurationMinutes());
        movie.setGenre(dto.getGenre());
        // Parse the ISO 8601 String from DTO into Firestore Timestamp
        movie.setReleaseDate(Timestamp.parseTimestamp(dto.getReleaseDate()));
        movie.setDirector(dto.getDirector());
        movie.setCast(dto.getCast());
        return movieRepository.save(movie); // Assuming save returns the saved movie with ID
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

    // === IMPLEMENT MISSING METHODS ===

    /**
     * Lấy danh sách phim đang chiếu (releaseDate <= now).
     */
    @Override
    public List<Movie> getNowShowingMovies() throws ExecutionException, InterruptedException {
        List<Movie> allMovies = movieRepository.getAll(); // Get all movies
        Timestamp now = Timestamp.now(); // Get current time as Firestore Timestamp

        // Filter the list: keep movies where releaseDate is not null AND is before or equal to now
        return allMovies.stream()
                .filter(movie -> movie.getReleaseDate() != null && !movie.getReleaseDate().toDate().after(now.toDate()))
                // Alternative comparison: movie.getReleaseDate().compareTo(now) <= 0
                .collect(Collectors.toList()); // Collect the filtered movies into a new list
    }

    /**
     * Lấy danh sách phim sắp chiếu (releaseDate > now).
     */
    @Override
    public List<Movie> getComingSoonMovies() throws ExecutionException, InterruptedException {
        List<Movie> allMovies = movieRepository.getAll(); // Get all movies
        Timestamp now = Timestamp.now(); // Get current time

        // Filter the list: keep movies where releaseDate is not null AND is after now
        return allMovies.stream()
                .filter(movie -> movie.getReleaseDate() != null && movie.getReleaseDate().toDate().after(now.toDate()))
                // Alternative comparison: movie.getReleaseDate().compareTo(now) > 0
                .collect(Collectors.toList()); // Collect the filtered movies
    }
    // === END IMPLEMENT MISSING METHODS ===
}