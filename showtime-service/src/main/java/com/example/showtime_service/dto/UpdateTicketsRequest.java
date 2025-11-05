package com.example.showtime_service.dto;

// Thêm các import này
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO này được BookingService (TV5) sử dụng để gọi sang
 * ShowtimeService (TV4) và yêu cầu cập nhật (trừ) số lượng vé.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketsRequest {
    private String showtimeId;
    private int quantityToChange; // Ví dụ: -2 (để trừ 2 vé)
}