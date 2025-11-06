package com.example.review_service.model;
import com.google.cloud.Timestamp;
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
    private double rating;
    private String comment;
    private Timestamp createdAt;
    private String userFullName;
}