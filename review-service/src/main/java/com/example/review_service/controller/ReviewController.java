package com.example.review_service.controller;


import com.example.review_service.dto.ReviewDto;
import com.example.review_service.model.Review;
import com.example.review_service.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody ReviewDto dto) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(reviewService.createReview(dto));
    }

    @GetMapping
    public ResponseEntity<List<Review>> getAll() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    // === ENDPOINT MỚI BẮT BUỘC PHẢI THÊM ===
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Review>> getReviewsByMovieId(@PathVariable String movieId) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(reviewService.getReviewsByMovieId(movieId));
    }
    // === KẾT THÚC ENDPOINT MỚI ===

    @GetMapping("/{id}")
    public ResponseEntity<Review> getById(@PathVariable String id) throws ExecutionException, InterruptedException {
        Review review = reviewService.getReviewById(id);
        return (review != null) ? ResponseEntity.ok(review) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
