package com.btlcnpm.TicketService.dto;

import lombok.Data;

@Data
public class TriggerTicketRequest {
    private String bookingId;
    private String userId;
}