package com.example.Movie_Catalog_Service.controller;

import org.springframework.web.bind.annotation.RequestParam; // Make sure this is imported
import com.example.Movie_Catalog_Service.dto.MovieDto;
import com.example.Movie_Catalog_Service.model.Movie;
import com.example.Movie_Catalog_Service.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/movies") // Base path is correct
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    public ResponseEntity<Movie> createMovie(@RequestBody MovieDto dto) throws ExecutionException, InterruptedException {
        // Assuming createMovie returns the created Movie object
        return ResponseEntity.ok(movieService.createMovie(dto));
    }

    // Handles GET requests like /api/v1/movies?status=now_showing
    @GetMapping
    public ResponseEntity<List<Movie>> getAll(@RequestParam(required = false) String status) throws ExecutionException, InterruptedException {
        List<Movie> movies; // Declare the list

        // Filter based on the status parameter
        if ("now_showing".equalsIgnoreCase(status)) {
            movies = movieService.getNowShowingMovies(); // Get currently showing movies
        } else if ("coming_soon".equalsIgnoreCase(status)) {
            movies = movieService.getComingSoonMovies(); // Get upcoming movies
        } else {
            // If status is null, empty, or something else, get all movies
            movies = movieService.getAllMovies();
        }

        // === FIX HERE ===
        // Return the filtered list 'movies' that was determined above
        return ResponseEntity.ok(movies); // <<< Return the correct list
    }

    // Handles GET requests like /api/v1/movies/m_001
    @GetMapping("/{id}")
    public ResponseEntity<Movie> getById(@PathVariable String id) throws ExecutionException, InterruptedException {
        Movie movie = movieService.getMovieById(id);
        // Return 200 OK with the movie if found, otherwise 404 Not Found
        return (movie != null) ? ResponseEntity.ok(movie) : ResponseEntity.notFound().build();
    }

    // Handles DELETE requests like /api/v1/movies/m_001
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        // Assuming deleteMovie doesn't throw errors for non-existent IDs
        movieService.deleteMovie(id);
        // Return 204 No Content to indicate successful deletion (or successful "no operation")
        return ResponseEntity.noContent().build();
    }
}