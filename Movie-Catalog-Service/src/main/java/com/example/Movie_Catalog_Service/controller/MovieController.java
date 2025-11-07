package com.example.Movie_Catalog_Service.controller;


import com.example.Movie_Catalog_Service.dto.MovieDto;
import com.example.Movie_Catalog_Service.dto.MovieResponseDto;
import com.example.Movie_Catalog_Service.model.Movie;
import com.example.Movie_Catalog_Service.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    public ResponseEntity<MovieResponseDto> createMovie(@RequestBody MovieDto dto) throws ExecutionException, InterruptedException {
        Movie createdMovie = movieService.createMovie(dto);
        return ResponseEntity.ok(new MovieResponseDto(createdMovie)); // Trả về DTO
    }

    @GetMapping
    public ResponseEntity<List<MovieResponseDto>> getAll(@RequestParam(required = false) String status) throws ExecutionException, InterruptedException {
        List<Movie> movies = movieService.getAllMovies();
        // Chuyển đổi List<Movie> thành List<MovieResponseDto>
        List<MovieResponseDto> movieDtos = MovieResponseDto.fromMovies(movies);
        return ResponseEntity.ok(movieDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponseDto> getById(@PathVariable String id) throws ExecutionException, InterruptedException {
        Movie movie = movieService.getMovieById(id);
        if (movie != null) {
            return ResponseEntity.ok(new MovieResponseDto(movie)); // Chuyển đổi sang DTO
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}
