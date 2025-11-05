package com.btlcnpm.BookingService.dto;

import lombok.Data;

@Data
public class CreateBookingRequest {

    // ID của suất chiếu mà người dùng chọn
    private String showtimeId;

    // Số lượng vé muốn mua
    private int quantity;
}