package com.btlcnpm.androidapp.data.model

// Data class cho response từ Movie Catalog Service
data class Movie(
    val movieId: String?,
    val title: String?,
    val description: String?,
    val posterUrl: String?, // Dùng cho Coil
    val backdropUrl: String?,
    val trailerUrl: String?,
    val durationMinutes: Int?,
    val genre: List<String>?,
    val releaseDate: String?, // Đã sửa lỗi: Backend trả về String ISO 8601
    val director: String?,
    val cast: List<String>?
)

// DTO cho Search (Dùng để nhất quán)
data class MovieSearchDTO(
    val movieId: String?,
    val title: String?,
    val posterUrl: String?
)