package com.example.review_service.service.impl;

import com.example.review_service.dto.ReviewDto;
import com.example.review_service.model.Review;
import com.example.review_service.repository.ReviewRepository;
import com.example.review_service.service.ReviewService;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final Firestore db;

    public ReviewServiceImpl(ReviewRepository reviewRepository, Firestore db) {
        this.reviewRepository = reviewRepository;
        this.db = db;
    }

    @Override
    public Review createReview(ReviewDto dto) throws ExecutionException, InterruptedException {
        Review review = new Review();
        review.setMovieId(dto.getMovieId());
        review.setUserId(dto.getUserId());
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setUserFullName(dto.getUserFullName());
        review.setCreatedAt(String.valueOf(Timestamp.now()));

        // Tự sinh reviewId nếu chưa có
        String newId = generateReviewId();
        review.setReviewId(newId);

        return reviewRepository.save(review);
    }

    private String generateReviewId() throws ExecutionException, InterruptedException {
        CollectionReference reviews = db.collection("reviews");
        ApiFuture<QuerySnapshot> future = reviews.get();
        List<QueryDocumentSnapshot> docs = future.get().getDocuments();
        int count = docs.size() + 1;

        // Tạo mã kiểu Rv1, Rv2, ...
        String candidateId;
        do {
            candidateId = "Rv" + count++;
        } while (isIdExists(candidateId));

        return candidateId;
    }

    private boolean isIdExists(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection("reviews").document(id).get().get();
        return doc.exists();
    }

    @Override
    public List<Review> getAllReviews() throws ExecutionException, InterruptedException {
        return reviewRepository.getAll();
    }

    // === THÊM TRIỂN KHAI CHO HÀM MỚI ===
    @Override
    public List<Review> getReviewsByMovieId(String movieId) throws ExecutionException, InterruptedException {
        return reviewRepository.findByMovieId(movieId);
    }
    // === KẾT THÚC ===

    @Override
    public Review getReviewById(String id) throws ExecutionException, InterruptedException {
        return reviewRepository.getById(id);
    }

    @Override
    public void deleteReview(String id) {
        reviewRepository.deleteById(id);
    }
}
