package com.btlcnpm.androidapp.data.model

// Data class cho response từ Review Service (tương ứng Review.java)
data class Review(
    val reviewId: String?,
    val movieId: String?,
    val userId: String?,
    val rating: Double?, // Backend dùng int
    val comment: String?,
    val createdAt: FirestoreTimestamp?, // Backend trả về String (Timestamp.now().toString())
    val userFullName: String?
)