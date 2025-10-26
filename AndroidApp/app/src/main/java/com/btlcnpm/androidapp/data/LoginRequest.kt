package com.btlcnpm.androidapp.data // Thay bằng package của bạn

// Data class cho dữ liệu gửi đi khi đăng nhập
data class LoginRequest(
    val email: String,    // Trường "email", kiểu String
    val password: String  // Trường "password", kiểu String
)