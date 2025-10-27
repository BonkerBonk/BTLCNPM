package com.btlcnpm.SearchService.controller;

import com.btlcnpm.SearchService.dto.MovieSearchDTO;
import com.btlcnpm.SearchService.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search") // Đây là đường dẫn gốc cho service này
public class SearchController {

    private final SearchService searchService;

    // Tiêm (Inject) lớp Service mà chúng ta vừa tạo
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    // Định nghĩa API: GET /api/v1/search/movies?q=...
    @GetMapping("/movies")
    public ResponseEntity<List<MovieSearchDTO>> searchMovies(@RequestParam("q") String query) {
        try {
            List<MovieSearchDTO> results = searchService.searchMovies(query);
            return ResponseEntity.ok(results); // Trả về 200 OK + danh sách phim
        } catch (Exception e) {
            e.printStackTrace();
            // Trả về lỗi 500 nếu có sự cố
            return ResponseEntity.internalServerError().build();
        }
    }
}