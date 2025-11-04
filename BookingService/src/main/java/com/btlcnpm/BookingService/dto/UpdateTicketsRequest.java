package com.btlcnpm.BookingService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO này dùng để gửi yêu cầu "Trừ vé" (bằng số âm)
 * sang cho ShowtimeService (TV4)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketsRequest {
    private String showtimeId;
    private int quantityToChange; // Ví dụ: -2 (để trừ 2 vé)
}

