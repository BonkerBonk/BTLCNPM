package com.btlcnpm.androidapp.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    // Hàm này sẽ gọi đến GET /api/v1/movie/movies trên ApiGateway
    @GET("/api/v1/movie/movies")
    suspend fun getMovies( // suspend fun cho phép chạy trong Coroutine
        @Query("status") status: String // Tham số ?status=now_showing hoặc ?status=coming_soon
    ): List<MovieDTO> // Hàm sẽ trả về một danh sách các đối tượng MovieDTO
    @POST("/api/v1/auth/login")
    suspend fun login( // suspend fun để chạy trong coroutine
        // @Body báo cho Retrofit lấy đối tượng 'request'
        // và chuyển thành JSON đặt vào body của HTTP request
        @Body request: LoginRequest
    ): LoginResponse // Kiểu dữ liệu trả về mong đợi (Retrofit tự parse JSON)
}