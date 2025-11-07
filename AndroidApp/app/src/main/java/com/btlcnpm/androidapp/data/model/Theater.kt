package com.btlcnpm.androidapp.data.model

// DTO cho Theater (Rạp chiếu)
// Các tên trường này phải khớp với JSON trả về từ theater-service

/**
 * Model này đại diện cho GeoPoint của Firestore.
 * Khi Jackson (thư viện JSON) serialize `com.google.cloud.firestore.GeoPoint`
 * nó thường trả về một object có chứa `latitude` và `longitude`.
 */
data class GeoPoint(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

/**
 * Model đại diện cho một rạp chiếu phim (Theater).
 * Các trường đều là nullable (?) để đảm bảo an toàn khi parse JSON.
 */
data class Theater(
    val theaterId: String?,
    val name: String?,
    val address: String?,
    val city: String?,
    val location: GeoPoint? // Dùng model GeoPoint đã định nghĩa ở trên
)

