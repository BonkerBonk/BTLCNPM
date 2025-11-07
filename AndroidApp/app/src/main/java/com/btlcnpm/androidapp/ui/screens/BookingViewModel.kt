// com/btlcnpm/androidapp/ui/screens/BookingViewModel.kt
package com.btlcnpm.androidapp.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
// import com.btlcnpm.androidapp.data.model.PaymentResponse // Không cần dùng PaymentResponse nữa
import com.btlcnpm.androidapp.data.model.Showtime // <<< THÊM IMPORT NÀY
import com.btlcnpm.androidapp.data.remote.ApiConfig
import com.btlcnpm.androidapp.data.repository.AuthRepository
import com.btlcnpm.androidapp.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Trạng thái cho màn hình chọn suất chiếu
sealed class ShowtimeUiState {
    // <<< SỬA LỖI 1: Các trạng thái này phải kế thừa từ ShowtimeUiState
    object Idle : ShowtimeUiState()
    object Loading : ShowtimeUiState()
    // <<< SỬA LỖI 2: Thêm trạng thái Success để chứa List<Showtime>
    data class Success(val showtimes: List<Showtime>) : ShowtimeUiState()
    data class Error(val message: String) : ShowtimeUiState()
}

// Trạng thái cho luồng thanh toán (màn hình chọn số lượng)
sealed class BookingUiState {
    object Idle : BookingUiState()
    object Loading : BookingUiState()
    data class AwaitingMomoPayment(val qrCodeUrl: String, val bookingId: String) : BookingUiState()
    data class AwaitingVnpayPayment(val payUrl: String, val bookingId: String) : BookingUiState()
    data class MockSuccess(val bookingId: String) : BookingUiState()
    data class Error(val message: String) : BookingUiState()
}

class BookingViewModel(
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // State cho màn hình Chọn Suất Chiếu
    private val _showtimeUiState = MutableStateFlow<ShowtimeUiState>(ShowtimeUiState.Idle)
    val showtimeUiState: StateFlow<ShowtimeUiState> = _showtimeUiState.asStateFlow()

    // State cho màn hình Chọn Số Lượng
    private val _bookingUiState = MutableStateFlow<BookingUiState>(BookingUiState.Idle)
    val bookingUiState: StateFlow<BookingUiState> = _bookingUiState.asStateFlow()

    /**
     * Lọc các suất chiếu cho 1 phim tại 1 rạp
     */
    fun loadShowtimes(movieId: String, theaterId: String) {
        viewModelScope.launch {
            _showtimeUiState.value = ShowtimeUiState.Loading

            val result = bookingRepository.getAllShowtimes()

            result.onSuccess { allShowtimes ->
                val filtered = allShowtimes.filter {
                    it.movieId == movieId && it.theaterId == theaterId
                }
                // <<< SỬA LỖI 3: Gán vào trạng thái Success của ShowtimeUiState
                _showtimeUiState.value = ShowtimeUiState.Success(filtered)

            }.onFailure { e ->
                _showtimeUiState.value = ShowtimeUiState.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }

    /**
     * Bắt đầu luồng tạo booking PENDING và lấy URL/QR thanh toán
     */
    fun startBookingFlow(showtimeId: String, quantity: Int, paymentMethod: String) {
        viewModelScope.launch {
            _bookingUiState.value = BookingUiState.Loading
            var bookingId: String? = null

            // 1. Tạo Booking
            val bookingResult = bookingRepository.createBooking(showtimeId, quantity)
            bookingResult.onSuccess { bookingResponse ->
                bookingId = bookingResponse.bookingId
            }.onFailure { e ->
                _bookingUiState.value = BookingUiState.Error(e.message ?: "Lỗi tạo Booking")
                return@launch
            }

            if (bookingId == null) {
                _bookingUiState.value = BookingUiState.Error("Không lấy được Booking ID")
                return@launch
            }

            // 2. Gọi ProcessPayment
            val paymentResult = bookingRepository.processPayment(bookingId!!, paymentMethod)

            paymentResult.onSuccess { responseMap -> // responseMap ở đây đang là kiểu Any?

                // Ép kiểu responseMap về Map<String, Any> một cách an toàn
                val map = responseMap as? Map<String, Any>

                if (paymentMethod == "MOMO_QR") {
                    // SỬA LỖI: Dùng map?.get("key")
                    val qrCodeUrl = map?.get("qrCodeUrl") as? String
                    if (qrCodeUrl != null) {
                        _bookingUiState.value = BookingUiState.AwaitingMomoPayment(qrCodeUrl, bookingId!!)
                    } else {
                        _bookingUiState.value = BookingUiState.Error("Không nhận được QR Code URL")
                    }
                } else if (paymentMethod == "VNPAY") {
                    // SỬA LỖI: Dùng map?.get("key")
                    val payUrl = map?.get("payUrl") as? String
                    if (payUrl != null) {
                        _bookingUiState.value = BookingUiState.AwaitingVnpayPayment(payUrl, bookingId!!)
                    } else {
                        _bookingUiState.value = BookingUiState.Error("Không nhận được URL thanh toán VNPay")
                    }
                } else if (paymentMethod == "MOCK_SUCCESS") {
                    _bookingUiState.value = BookingUiState.MockSuccess(bookingId!!)
                }
            }.onFailure { e ->
                _bookingUiState.value = BookingUiState.Error(e.message ?: "Lỗi khi gọi thanh toán")
            }
        }
    }

    /**
     * Bắt đầu polling (lặp lại) kiểm tra xem vé đã được tạo hay chưa
     */
    fun startPollingBookingStatus(bookingId: String) {
        viewModelScope.launch {
            if (_bookingUiState.value !is BookingUiState.Loading) {
                _bookingUiState.value = BookingUiState.Loading
            }

            var attempts = 0
            val maxAttempts = 20 // Poll trong 1 phút (20 lần * 3 giây)

            while (attempts < maxAttempts) {
                kotlinx.coroutines.delay(3000) // Chờ 3 giây

                val ticketResult = authRepository.getMyTickets()

                ticketResult.onSuccess { tickets ->
                    val ticketExists = tickets.any { it.bookingId == bookingId }
                    if (ticketExists) {
                        // TÌM THẤY VÉ! -> Thanh toán thành công
                        _bookingUiState.value = BookingUiState.MockSuccess(bookingId)
                        return@launch // Thoát vòng lặp
                    }
                }.onFailure { e ->
                    Log.e("BookingVM", "Polling error: ${e.message}")
                }
                attempts++
            }

            // Hết thời gian mà không thấy vé
            _bookingUiState.value = BookingUiState.Error("Thanh toán hết hạn hoặc thất bại. Vui lòng thử lại.")
        }
    }

    fun resetBookingState() {
        _bookingUiState.value = BookingUiState.Idle
    }

    fun resetShowtimeState() {
        _showtimeUiState.value = ShowtimeUiState.Idle
    }

    // Factory
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(BookingViewModel::class.java)) {
                    val repository = ApiConfig.bookingRepository
                    val authRepo = ApiConfig.authRepository
                    return BookingViewModel(repository, authRepo) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}