package com.btlcnpm.androidapp.data // Thay bằng package của bạn

// Data class cho dữ liệu nhận về sau khi đăng nhập thành công
data class LoginResponse(
    val userId: String, // Trường "userId"
    val token: String   // Trường "token" chứa JWT Token
)