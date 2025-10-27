package com.btlcnpm.androidapp.data

import android.util.Log
import okhttp3.Interceptor // Import từ thư viện OkHttp
import okhttp3.Response

// Lớp Interceptor kế thừa từ okhttp3.Interceptor
class AuthInterceptor : Interceptor {

    // Hàm intercept sẽ được gọi cho MỌI request đi qua OkHttpClient
    override fun intercept(chain: Interceptor.Chain): Response {
        Log.d("AuthInterceptor", "Intercepting request for URL: ${chain.request().url}")
        // Lấy request gốc
        val originalRequest = chain.request()
        // Tạo một request builder mới dựa trên request gốc
        val requestBuilder = originalRequest.newBuilder()

        // Lấy token từ TokenManager
        val token = TokenManager.getToken()

        // Nếu có token
        if (token != null) {
            Log.d("AuthInterceptor", "Adding Authorization header.")
            // Thêm header "Authorization" với giá trị "Bearer <token>"
            // Đây là định dạng chuẩn cho JWT
            requestBuilder.addHeader("Authorization", "Bearer $token")
        } else {
            Log.d("AuthInterceptor", "No token found, proceeding without Authorization header.")
        }

        // Xây dựng request mới (có thể đã có header hoặc không)
        val newRequest = requestBuilder.build()
        // Thực hiện request mới và trả về response
        return chain.proceed(newRequest)
    }
}