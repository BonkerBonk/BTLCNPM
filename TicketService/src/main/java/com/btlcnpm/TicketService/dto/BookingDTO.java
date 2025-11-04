package com.btlcnpm.TicketService.dto;

import lombok.Data;

@Data
public class BookingDTO {
    // Đây là những thông tin ta cần từ BookingService
    private int quantity;
    private String showtimeId;
    private String userId;
    // Chúng ta sẽ cần gọi thêm các service khác để lấy movieTitle, v.v.
    // nhưng tạm thời chỉ cần quantity
}