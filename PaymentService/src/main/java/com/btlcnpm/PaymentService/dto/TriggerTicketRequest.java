package com.btlcnpm.PaymentService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TriggerTicketRequest {

    private String bookingId;
    private String userId;
}