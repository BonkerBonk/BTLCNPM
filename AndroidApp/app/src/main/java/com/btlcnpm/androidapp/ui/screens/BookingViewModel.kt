package com.btlcnpm.androidapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.btlcnpm.androidapp.data.model.PaymentResponse
import com.btlcnpm.androidapp.data.model.Showtime
import com.btlcnpm.androidapp.data.remote.ApiConfig
import com.btlcnpm.androidapp.data.repository.AuthRepository
import com.btlcnpm.androidapp.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Trạng thái cho màn hình chọn suất chiếu
sealed class ShowtimeUiState {
    object Idle : ShowtimeUiState()
    object Loading : ShowtimeUiState()
    data class Success(val showtimes: List<Showtime>) : ShowtimeUiState()
    data class Error(val message: String) : ShowtimeUiState()
}

// Trạng thái cho luồng thanh toán (màn hình chọn số lượng)
sealed class BookingUiState {
    object Idle : BookingUiState()
    object Loading : BookingUiState()
    // Sửa lại: Dùng data class PaymentResponse
    data class Success(val paymentResponse: PaymentResponse) : BookingUiState()
    data class Error(val message: String) : BookingUiState()
}

class BookingViewModel(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    // State cho màn hình Chọn Suất Chiếu
    private val _showtimeUiState = MutableStateFlow<ShowtimeUiState>(ShowtimeUiState.Idle)
    val showtimeUiState: StateFlow<ShowtimeUiState> = _showtimeUiState.asStateFlow()

    // State cho màn hình Chọn Số Lượng
    private val _bookingUiState = MutableStateFlow<BookingUiState>(BookingUiState.Idle)
    val bookingUiState: StateFlow<BookingUiState> = _bookingUiState.asStateFlow()

    /**
     * HÀM ĐÃ SỬA LẠI (để fix lỗi trong ảnh)
     * Lọc các suất chiếu cho 1 phim tại 1 rạp
     */
    fun loadShowtimes(movieId: String, theaterId: String) {
        // 1. Bắt đầu một coroutine
        viewModelScope.launch {
            // 2. Đặt trạng thái Loading
            _showtimeUiState.value = ShowtimeUiState.Loading

            // 3. Gọi suspend function và nhận kết quả
            val result = bookingRepository.getAllShowtimes() // Hàm này trả về Result<...>

            // 4. Xử lý kết quả (Result) thành công hoặc thất bại
            result.onSuccess { allShowtimes ->
                // Lọc dữ liệu (logic này đã đúng)
                val filtered = allShowtimes.filter {
                    it.movieId == movieId && it.theaterId == theaterId
                }
                // 5. Cập nhật UI khi thành công
                _showtimeUiState.value = ShowtimeUiState.Success(filtered)

            }.onFailure { e ->
                // 6. Cập nhật UI khi thất bại
                _showtimeUiState.value = ShowtimeUiState.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }

    /**
     * HÀM ĐÃ SỬA LẠI (để fix lỗi trong ảnh)
     * Thực hiện luồng đặt vé
     */
    fun startBookingFlow(showtimeId: String, quantity: Int) {
        viewModelScope.launch {
            _bookingUiState.value = BookingUiState.Loading
            var bookingId: String? = null

            // 1. Tạo Booking
            val bookingResult = bookingRepository.createBooking(showtimeId, quantity)

            bookingResult.onSuccess { bookingResponse ->
                bookingId = bookingResponse.bookingId
            }.onFailure { e ->
                _bookingUiState.value = BookingUiState.Error(e.message ?: "Lỗi tạo Booking")
                return@launch // Dừng luồng
            }

            // 2. Thanh toán
            if (bookingId != null) {
                val paymentResult = bookingRepository.processPayment(bookingId!!)

                paymentResult.onSuccess { paymentResponse ->
                    _bookingUiState.value = BookingUiState.Success(paymentResponse)
                }.onFailure { e ->
                    _bookingUiState.value = BookingUiState.Error(e.message ?: "Lỗi thanh toán")
                }
            } else {
                _bookingUiState.value = BookingUiState.Error("Không lấy được Booking ID")
            }
        }
    }

    // Reset state khi hoàn thành
    fun resetBookingState() {
        _bookingUiState.value = BookingUiState.Idle
    }

    // Factory
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(BookingViewModel::class.java)) {
                    // SỬA LẠI: Lấy repository từ ApiConfig
                    // (ApiConfig.bookingRepository đã tự động có AuthRepository)
                    val repository = ApiConfig.bookingRepository
                    return BookingViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}