package com.btlcnpm.androidapp.data.model

// Data class cho request body của API Login
// Tên trường phải khớp chính xác với JSON API yêu cầu
data class LoginRequest(
    val email: String,
    val password: String
)

// Data class cho response body của API Login và Register
data class AuthResponse(
    val userId: String,
    val token: String // JWT Token
)

// Data class cho request body của API Register (tương ứng RegisterRequest.java)
data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String
)

// Data class cho response body của API GET /profile/me (tương ứng UserProfileResponse.java)
// Các trường nullable vì Firestore có thể không có dữ liệu
data class UserProfile(
    val userId: String?,
    val email: String?,
    val fullName: String? = null,
    val phoneNumber: String? = null,
    val profileImageUrl: String? = null,
    val dateOfBirth: String? = null // API trả về dạng String "YYYY-MM-DD"
)

// Data class cho request body của API PUT /profile/me (tương ứng UpdateProfileRequest.java)
data class UpdateProfileRequest(
    val fullName: String?,
    val phoneNumber: String?,
    val dateOfBirth: String? // Gửi đi dạng String "YYYY-MM-DD"
)

data class ForgotPasswordRequest(
    val email: String
)

// === THÊM CLASS NÀY VÀO CUỐI FILE ===
// Data class cho response body của API Register
// Khớp với RegisterResponse.java
data class RegisterResponse(
    val userId: String,
    val email: String,
    val fullName: String
)
