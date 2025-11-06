package com.btlcnpm.androidapp.data.repository

import android.util.Log
import com.btlcnpm.androidapp.data.model.*
import com.btlcnpm.androidapp.data.remote.BetaCinemaApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

// Repository cho luồng đặt vé, thống nhất dùng suspend fun trả về Result
class BookingRepository(
    private val apiService: BetaCinemaApi,
    private val authRepository: AuthRepository // <<< THÊM DÒNG NÀY
) {

    /**
     * Lấy tất cả suất chiếu.
     */
    suspend fun getAllShowtimes(): Result<List<Showtime>> {
        return withContext(Dispatchers.IO) {
            try {
                // Gọi API (đã thêm ở bước trước)
                val response = apiService.getAllShowtimes()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Log.e("BookingRepo", "getAllShowtimes failed: ${response.code()}")
                    Result.failure(HttpException(response))
                }
            } catch (e: Exception) {
                Log.e("BookingRepo", "getAllShowtimes error: ${e.message}")
                Result.failure(e)
            }
        }
    }

    /**
     * Tạo đơn hàng "PENDING".
     */
    suspend fun createBooking(showtimeId: String, quantity: Int): Result<BookingResponse> {
        // 1. Lấy token từ AuthRepository
        val token = authRepository.authToken
        if (token == null) {
            return Result.failure(IllegalStateException("User not logged in."))
        }
        val formattedToken = "Bearer $token"

        // 2. Tiếp tục code như cũ
        return withContext(Dispatchers.IO) {
            val request = BookingRequest(showtimeId = showtimeId, quantity = quantity)
            try {
                // 3. Truyền token (formattedToken) vào API
                val response = apiService.createBooking(formattedToken, request)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("BookingRepo", "Create booking failed: ${response.code()} - $errorMsg")
                    Result.failure(Exception(errorMsg)) // Trả về lỗi từ server
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Xử lý thanh toán.
     */
    suspend fun processPayment(bookingId: String): Result<PaymentResponse> {
        return withContext(Dispatchers.IO) {
            // Hard-code "MOCK_SUCCESS" để luôn thành công
            val request = CheckoutRequest(bookingId = bookingId, paymentMethod = "MOCK_SUCCESS")
            try {
                val response = apiService.processPayment(request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("BookingRepo", "Payment failed: ${response.code()} - $errorMsg")
                    Result.failure(Exception("Lỗi thanh toán: $errorMsg"))
                }
            } catch (e: Exception) {
                Log.e("BookingRepo", "Payment error: ${e.message}")
                Result.failure(e)
            }
        }
    }
}