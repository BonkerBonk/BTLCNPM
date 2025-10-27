package com.btlcnpm.androidapp.data

// Repository nhận ApiService (ở đây lấy trực tiếp từ Singleton,
// sau này nên dùng Dependency Injection như Hilt/Koin)
class MovieRepository(private val apiService: ApiService = RetrofitInstance.api) {

    // Hàm lấy phim đang chiếu, gọi hàm tương ứng trong ApiService
    // suspend fun đánh dấu hàm này phải được gọi từ Coroutine
    suspend fun getNowShowingMovies(): List<MovieDTO> {
        return apiService.getMovies(status = "now_showing")
    }

    // Hàm lấy phim sắp chiếu
    suspend fun getComingSoonMovies(): List<MovieDTO> {
        return apiService.getMovies(status = "coming_soon")
    }
}