package com.example.payment_service.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// DTO này dùng để hứng dữ liệu (chỉ lấy totalAmount)
// trả về từ BookingService
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingDTO {

    private double totalAmount;
    private String userId; // <<< THÊM TRƯỜNG NÀY

    // Getter
    public double getTotalAmount() {
        return totalAmount;
    }
    public String getUserId() { // <<< THÊM GETTER NÀY
        return userId;
    }

    // Setter
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    public void setUserId(String userId) { // <<< THÊM SETTER NÀY
        this.userId = userId;
    }
}