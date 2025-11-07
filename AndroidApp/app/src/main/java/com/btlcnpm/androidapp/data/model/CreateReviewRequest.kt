package com.btlcnpm.androidapp.data.model

/**
 * Data class này (DTO) được dùng để gửi thông tin
 * khi người dùng TẠO MỘT REVIEW MỚI.
 * Nó khớp với `ReviewDto.java` trong `review-service`.
 */
data class CreateReviewRequest(
    val movieId: String,
    val userId: String,
    val rating: Int,
    val comment: String,
    val userFullName: String
)

