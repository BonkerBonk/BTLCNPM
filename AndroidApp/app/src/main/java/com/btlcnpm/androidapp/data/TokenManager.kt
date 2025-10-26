package com.btlcnpm.androidapp.data

import android.util.Log

// Singleton object để quản lý token
object TokenManager {
    private var currentToken: String? = null // Biến lưu token trong bộ nhớ

    // Hàm để lưu token (được gọi từ AuthViewModel sau khi login thành công)
    fun saveToken(token: String) {
        Log.d("TokenManager", "Saving token: ${token.take(10)}...") // Log 10 ký tự đầu
        currentToken = token
    }

    // Hàm để lấy token (được gọi từ AuthInterceptor)
    fun getToken(): String? {
        Log.d("TokenManager", "Getting token: ${currentToken?.take(10)}...")
        return currentToken
    }

    // Hàm để xóa token (được gọi khi đăng xuất)
    fun clearToken() {
        Log.d("TokenManager", "Clearing token.")
        currentToken = null
    }
}