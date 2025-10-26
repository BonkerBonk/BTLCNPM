package com.btlcnpm.androidapp.data

// Repository nhận vào ApiService (tạm thời lấy từ Singleton RetrofitInstance)
// Sau này nên dùng Dependency Injection (Hilt/Koin) để cung cấp ApiService
class AuthRepository(private val apiService: ApiService = RetrofitInstance.api) {

    // Hàm login của Repository chỉ đơn giản là gọi hàm login của ApiService
    // suspend fun vì nó gọi một suspend fun khác
    suspend fun login(request: LoginRequest): LoginResponse {
        // Gọi hàm login đã khai báo trong ApiService
        return apiService.login(request)
    }

    // (Thêm hàm register, forgotPassword... ở đây sau)
}