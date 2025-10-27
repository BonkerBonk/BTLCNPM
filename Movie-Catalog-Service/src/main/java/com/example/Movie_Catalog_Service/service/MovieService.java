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

    // === THÊM CÁC HÀM CÒN THIẾU ===
    /**
     * Lấy danh sách phim đang chiếu.
     * @return Danh sách phim.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    List<Movie> getNowShowingMovies() throws ExecutionException, InterruptedException;

    /**
     * Lấy danh sách phim sắp chiếu.
     * @return Danh sách phim.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    List<Movie> getComingSoonMovies() throws ExecutionException, InterruptedException;
    // === KẾT THÚC THÊM HÀM ===
}