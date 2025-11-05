package com.example.review_service.dto;

import lombok.Data;

@Data
public class ReviewDto {
    private String movieId;
    private String userId;
    private int rating;
    private String comment;
    private String userFullName;
}