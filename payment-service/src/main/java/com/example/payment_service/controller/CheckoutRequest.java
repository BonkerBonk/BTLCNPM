package com.example.payment_service.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// DTO này dùng để hứng dữ liệu (bookingId, paymentMethod)
// gửi lên từ ứng dụng Android khi gọi API /checkout
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckoutRequest {
    private String bookingId;
    private String paymentMethod; // "VNPAY", "MOMO_QR", "MOCK_SUCCESS"

    // Getters
    public String getBookingId() {
        return bookingId;
    }
    public String getPaymentMethod() {
        return paymentMethod;
    }

    // Setters
    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}