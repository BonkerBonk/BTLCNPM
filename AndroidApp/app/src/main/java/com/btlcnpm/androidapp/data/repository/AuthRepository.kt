package com.btlcnpm.androidapp.data.repository

import android.util.Log // Import Log
import com.btlcnpm.androidapp.data.model.*
import com.btlcnpm.androidapp.data.remote.BetaCinemaApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException // Import HttpException
import java.io.IOException // Import IOException

// Repository để xử lý các tác vụ liên quan đến Auth và Profile
class AuthRepository(private val apiService: BetaCinemaApi) {

    // Nơi lưu trữ token tạm thời (Trong ứng dụng thực tế nên dùng DataStore/SharedPreferences)
    var authToken: String? = null
        private set // Chỉ cho phép đọc từ bên ngoài

    // Hàm thực hiện đăng nhập
    suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return withContext(Dispatchers.IO) { // Chuyển sang IO thread để gọi mạng
            try {
                val response = apiService.login(request)
                if (response.isSuccessful && response.body() != null) {
                    authToken = response.body()?.token // Lưu token khi thành công
                    Log.d("AuthRepository", "Login successful, token saved.")
                    Result.success(response.body()!!)
                } else {
                    // Xử lý lỗi từ server (VD: Sai mật khẩu - 401 Unauthorized)
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("AuthRepository", "Login failed: ${response.code()} - $errorBody")
                    Result.failure(HttpException(response)) // Trả về HttpException
                }
            } catch (e: IOException) {
                // Lỗi mạng (Không có kết nối, timeout,...)
                Log.e("AuthRepository", "Network error during login: ${e.message}")
                Result.failure(e)
            } catch (e: Exception) {
                // Các lỗi khác (VD: Lỗi parse JSON)
                Log.e("AuthRepository", "Unexpected error during login: ${e.message}")
                Result.failure(e)
            }
        }
    }

    // Hàm thực hiện đăng ký
    suspend fun register(request: RegisterRequest): Result<RegisterResponse> { // <<< SỬA 1: Kiểu trả về
        return withContext(Dispatchers.IO) {
            try {
                // Gọi API register mới
                val response = apiService.register(request)

                if (response.isSuccessful && response.body() != null) {
                    // KHÔNG LƯU TOKEN NỮA (vì backend không trả về token)
                    // authToken = response.body()?.token // <<< XÓA DÒNG NÀY

                    Log.d("AuthRepository", "Registration successful.")
                    Result.success(response.body()!!) // <<< SỬA 2: Trả về RegisterResponse
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Registration failed"
                    Log.e("AuthRepository", "Registration failed: ${response.code()} - $errorBody")
                    Result.failure(HttpException(response))
                }
            } catch (e: IOException) {
                Log.e("AuthRepository", "Network error during registration: ${e.message}")
                Result.failure(e)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Unexpected error during registration: ${e.message}")
                Result.failure(e)
            }
        }
    }


    // Hàm lấy thông tin Profile
    suspend fun getMyProfile(): Result<UserProfile> {
        val token = authToken // Lấy token đã lưu
        if (token == null) {
            Log.w("AuthRepository", "Attempted to get profile without token.")
            return Result.failure(IllegalStateException("User not logged in."))
        }
        // Thêm "Bearer " vào trước token
        val formattedToken = "Bearer $token"

        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMyProfile(formattedToken)
                if (response.isSuccessful && response.body() != null) {
                    Log.d("AuthRepository", "Profile fetched successfully.")
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Failed to fetch profile"
                    Log.e("AuthRepository", "Get profile failed: ${response.code()} - $errorBody")
                    Result.failure(HttpException(response))
                }
            } catch (e: IOException) {
                Log.e("AuthRepository", "Network error getting profile: ${e.message}")
                Result.failure(e)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Unexpected error getting profile: ${e.message}")
                Result.failure(e)
            }
        }
    }

    // Hàm cập nhật Profile
    suspend fun updateMyProfile(request: UpdateProfileRequest): Result<UserProfile> {
        val token = authToken
        if (token == null) {
            Log.w("AuthRepository", "Attempted to update profile without token.")
            return Result.failure(IllegalStateException("User not logged in."))
        }
        val formattedToken = "Bearer $token"

        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateMyProfile(formattedToken, request)
                if (response.isSuccessful && response.body() != null) {
                    Log.d("AuthRepository", "Profile updated successfully.")
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Failed to update profile"
                    Log.e("AuthRepository", "Update profile failed: ${response.code()} - $errorBody")
                    Result.failure(HttpException(response))
                }
            } catch (e: IOException) {
                Log.e("AuthRepository", "Network error updating profile: ${e.message}")
                Result.failure(e)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Unexpected error updating profile: ${e.message}")
                Result.failure(e)
            }
        }
    }

    suspend fun forgotPassword(email: String): Result<String> { // Trả về message String
        return withContext(Dispatchers.IO) {
            try {
                val request = ForgotPasswordRequest(email)
                val response = apiService.forgotPassword(request)
                if (response.isSuccessful && response.body() != null) {
                    val message = response.body()?.get("message") ?: "Yêu cầu đã được gửi." // Lấy message từ Map
                    Log.d("AuthRepository", "Forgot password request successful: $message")
                    Result.success(message)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Forgot password failed"
                    Log.e("AuthRepository", "Forgot password failed: ${response.code()} - $errorBody")
                    Result.failure(HttpException(response))
                }
            } catch (e: IOException) {
                Log.e("AuthRepository", "Network error during forgot password: ${e.message}")
                Result.failure(e)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Unexpected error during forgot password: ${e.message}")
                Result.failure(e)
            }
        }
    }

    // HÀM MỚI: Lấy vé của tôi
    suspend fun getMyTickets(): Result<List<Ticket>> {
        val token = authToken
        if (token == null) {
            return Result.failure(IllegalStateException("User not logged in."))
        }
        val formattedToken = "Bearer $token"

        return withContext(Dispatchers.IO) {
            try {
                // Gọi API mới
                val response = apiService.getMyTickets(formattedToken)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Log.e("AuthRepo", "Get tickets failed: ${response.code()}")
                    Result.failure(HttpException(response))
                }
            } catch (e: Exception) {
                Log.e("AuthRepo", "Get tickets error: ${e.message}")
                Result.failure(e)
            }
        }
    }

    // Hàm đăng xuất (đơn giản là xóa token)
    fun logout() {
        authToken = null
        Log.i("AuthRepository", "User logged out.")
    }
}