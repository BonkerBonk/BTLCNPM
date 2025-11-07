package com.btlcnpm.androidapp.data.remote

import com.btlcnpm.androidapp.data.model.*
import retrofit2.Response // Dùng Response<> để xử lý lỗi tốt hơn
import retrofit2.http.*

interface BetaCinemaApi {

    // --- AUTH SERVICE (Port 8080) ---
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse> // Dùng Response<>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<Map<String, String>> // Dùng Map vì response đơn giản

    // --- PROFILE SERVICE (Port 8080) ---
    @GET("profile/me")
    suspend fun getMyProfile(
        @Header("Authorization") token: String // VD: "Bearer <token>"
    ): Response<UserProfile>

    @PUT("profile/me")
    suspend fun updateMyProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<UserProfile>

    // --- MOVIE SERVICE (Port 8080) ---
    @GET("movies")
    suspend fun getAllMovies(): Response<List<Movie>>

    @GET("movies/{id}")
    suspend fun getMovieById(@Path("id") movieId: String): Response<Movie>

    // --- SEARCH SERVICE (Port 8080) ---
    @GET("search/movies")
    suspend fun searchMovies(@Query("q") query: String): Response<List<MovieSearchDTO>>

    // --- REVIEW SERVICE (Port 8087) ---
    // Sửa lại đường dẫn để khớp với API mới (Bước 1) và dùng URL tuyệt đối
    @GET("http://10.0.2.2:8087/api/v1/reviews/movie/{movieId}")
    suspend fun getReviewsByMovieId(@Path("movieId") movieId: String): Response<List<Review>>

    // Endpoint để tạo review mới
    @POST("http://10.0.2.2:8087/api/v1/reviews")
    suspend fun createReview(@Body request: CreateReviewRequest): Response<Review>


    // --- THEATER SERVICE (Port 8086) ---
    // Thêm API cho rạp chiếu, dùng URL tuyệt đối
    @GET("http://10.0.2.2:8086/api/v1/theaters")
    suspend fun getAllTheaters(): Response<List<Theater>>

    @GET("http://10.0.2.2:8086/api/v1/theaters/city/{city}")
    suspend fun getTheatersByCity(@Path("city") city: String): Response<List<Theater>>

    // --- SHOWTIME SERVICE (Port 8089) ---
    // API: GET /api/v1/showtime/showtimes
    @GET("http://10.0.2.2:8089/api/v1/showtime/showtimes")
    suspend fun getAllShowtimes(): Response<List<Showtime>>

    // --- BOOKING SERVICE (Port 8091) ---
    // API: POST /api/v1/booking/bookings
    @POST("http://10.0.2.2:8091/api/v1/booking/bookings")
    suspend fun createBooking(
        @Header("Authorization") token: String, // <<< THÊM DÒNG NÀY
        @Body request: BookingRequest
    ): Response<BookingResponse>

    // --- PAYMENT SERVICE (Port 8092) ---
    // API: POST /api/v1/payment/checkout
    @POST("http://10.0.2.2:8092/api/v1/payment/checkout")
    suspend fun processPayment(
        @Body request: CheckoutRequest
    ): Response<Map<String, Any>>

    // --- TICKET SERVICE (Port 8093) ---
    // API: GET /api/v1/ticket/my-tickets
    @GET("http://10.0.2.2:8093/api/v1/ticket/my-tickets")
    suspend fun getMyTickets(
        @Header("Authorization") token: String // Cần gửi token để xác thực
    ): Response<List<Ticket>>

}

