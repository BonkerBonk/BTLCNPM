package com.example.Movie_Catalog_Service.controller;


import com.example.Movie_Catalog_Service.dto.MovieDto;
import com.example.Movie_Catalog_Service.model.Movie;
import com.example.Movie_Catalog_Service.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    public ResponseEntity<Movie> createMovie(@RequestBody MovieDto dto) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(movieService.createMovie(dto));
    }

    @GetMapping
    public ResponseEntity<List<Movie>> getAll(@RequestParam(required = false) String status) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Movie> getById(@PathVariable String id) throws ExecutionException, InterruptedException {
        Movie movie = movieService.getMovieById(id);
        return (movie != null) ? ResponseEntity.ok(movie) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}
