package com.example.review_service.service;


import com.example.review_service.dto.ReviewDto;
import com.example.review_service.model.Review;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ReviewService {
    Review createReview(ReviewDto dto) throws ExecutionException, InterruptedException;
    List<Review> getAllReviews() throws ExecutionException, InterruptedException;
    Review getReviewById(String id) throws ExecutionException, InterruptedException;
    void deleteReview(String id);
}
