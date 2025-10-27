package com.btlcnpm.androidapp.data // Thay bằng package của bạn

// Data class tự động tạo getters, setters, equals, hashCode, toString
data class MovieDTO(
    val movieId: String,   // ID phim
    val title: String,     // Tên phim
    val posterUrl: String, // URL ảnh poster
    val genre: List<String>? // Danh sách thể loại (có thể null nếu API không trả về)
)