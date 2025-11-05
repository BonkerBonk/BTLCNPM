package com.example.review_service.repository;

import com.example.review_service.model.Review;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class ReviewRepository {

    private static final String COLLECTION_NAME = "reviews";

    public Review save(Review review) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        // Nếu reviewId chưa có thì generate
        if (review.getReviewId() == null || review.getReviewId().isEmpty()) {
            review.setReviewId(generateNextId());
        }

        // Save với ID rõ ràng
        db.collection(COLLECTION_NAME)
                .document(review.getReviewId())
                .set(review);
        return review;
    }

    public List<Review> getAll() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> query = db.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = query.get().getDocuments();
        List<Review> reviews = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            reviews.add(doc.toObject(Review.class));
        }
        return reviews;
    }

    // === THÊM HÀM MỚI ĐỂ TÌM THEO MOVIE ID ===
    public List<Review> findByMovieId(String movieId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> query = db.collection(COLLECTION_NAME)
                .whereEqualTo("movieId", movieId) // Tìm tất cả document có trường movieId khớp
                .get();
        List<QueryDocumentSnapshot> documents = query.get().getDocuments();
        List<Review> reviews = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            reviews.add(doc.toObject(Review.class));
        }
        return reviews;
    }
    // === KẾT THÚC HÀM MỚI ===

    public Review getById(String id) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(id).get().get();
        if (snapshot.exists()) {
            return snapshot.toObject(Review.class);
        }
        return null;
    }

    public void deleteById(String id) {
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME).document(id).delete();
    }

    // ✅ Generate next review ID: Rv1, Rv2, ...
    public String generateNextId() throws ExecutionException, InterruptedException {
        List<Review> reviews = getAll();
        int maxId = 0;
        for (Review review : reviews) {
            if (review.getReviewId() != null && review.getReviewId().startsWith("Rv")) {
                try {
                    int num = Integer.parseInt(review.getReviewId().substring(2));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return "Rv" + (maxId + 1);
    }
}
