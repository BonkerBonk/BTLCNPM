package com.btlcnpm.androidapp.data.model

// Dựa trên ShowtimeResponse.java từ showtime-service
data class Showtime(
    val showtimeId: String,
    val movieId: String,
    val theaterId: String,
    val roomId: String,
    val startTime: String, // Chuỗi ISO 8601 (ví dụ: "2025-10-26T19:00:00Z")
    val ticketPrice: Double,
    val availableTickets: Int
)