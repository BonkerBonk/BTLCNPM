package com.example.review_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private String reviewId;
    private String movieId;
    private String userId;
    private int rating;
    private String comment;
    private String createdAt;
    private String userFullName;
}