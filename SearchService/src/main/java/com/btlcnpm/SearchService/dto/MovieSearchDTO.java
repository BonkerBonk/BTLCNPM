package com.btlcnpm.SearchService.dto;

import lombok.Data;

@Data // <-- Chú thích @Data của Lombok sẽ tự tạo getters, setters, constructor...
public class MovieSearchDTO {
    // Các trường này phải khớp với tên trường trong collection 'movies' của bạn
    private String movieId;
    private String title;
    private String posterUrl;
}