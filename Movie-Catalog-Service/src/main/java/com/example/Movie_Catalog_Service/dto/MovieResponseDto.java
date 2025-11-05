package com.example.Movie_Catalog_Service.dto;

import com.example.Movie_Catalog_Service.model.Movie;
import com.google.cloud.Timestamp;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

@Data
@NoArgsConstructor
public class MovieResponseDto {
    private String movieId;
    private String title;
    private String description;
    private String posterUrl;
    private String backdropUrl;
    private String trailerUrl;
    private int durationMinutes;
    private List<String> genre;
    private String releaseDate; // Kiểu String ISO 8601
    private String director;
    private List<String> cast;

    // Định dạng ngày tháng chuẩn ISO 8601 UTC
    private static final String DATE_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    // Constructor để chuyển đổi từ Movie model sang DTO
    public MovieResponseDto(Movie movie) {
        this.movieId = movie.getMovieId();
        this.title = movie.getTitle();
        this.description = movie.getDescription();
        this.posterUrl = movie.getPosterUrl();
        this.backdropUrl = movie.getBackdropUrl();
        this.trailerUrl = movie.getTrailerUrl();
        this.durationMinutes = movie.getDurationMinutes();
        this.genre = movie.getGenre();
        this.director = movie.getDirector();
        this.cast = movie.getCast();

        // Chuyển đổi Timestamp sang String ISO 8601
        if (movie.getReleaseDate() != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_ISO);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Đảm bảo múi giờ UTC
                this.releaseDate = sdf.format(movie.getReleaseDate().toDate());
            } catch (Exception e) {
                // Xử lý nếu có lỗi format (ví dụ: đặt là null hoặc log lỗi)
                this.releaseDate = null;
                System.err.println("Error formatting releaseDate for movie " + movie.getMovieId() + ": " + e.getMessage());
            }
        } else {
            this.releaseDate = null;
        }
    }

    // Phương thức tĩnh để chuyển đổi một danh sách Movie
    public static List<MovieResponseDto> fromMovies(List<Movie> movies) {
        return movies.stream().map(MovieResponseDto::new).toList();
    }
}