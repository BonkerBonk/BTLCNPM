package com.btlcnpm.androidapp.data.model

// Dùng để GỬI đi khi tạo booking
data class BookingRequest(
    val showtimeId: String,
    val quantity: Int
)

// Dùng để NHẬN về sau khi tạo booking
data class BookingResponse(
    val bookingId: String,
    val userId: String,
    val showtimeId: String,
    val quantity: Int,
    val totalAmount: Double,
    val status: String
)

// Dùng để GỬI đi khi thanh toán
data class CheckoutRequest(
    val bookingId: String,
    val paymentMethod: String // Sẽ dùng "MOCK_SUCCESS"
)

// Dùng để NHẬN về sau khi thanh toán
// (Backend trả về Map<String, String>, ta tạo data class cho rõ ràng)
data class PaymentResponse(
    val bookingId: String,
    val status: String,
    val message: String
)