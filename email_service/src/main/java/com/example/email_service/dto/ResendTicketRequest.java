package com.example.email_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // Cần cho Jackson (để đọc JSON)
public class ResendTicketRequest {

    // Tên biến phải khớp với Hợp đồng API
    private String bookingId;
}