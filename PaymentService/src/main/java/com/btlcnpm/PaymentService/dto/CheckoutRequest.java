package com.btlcnpm.PaymentService.dto;

import lombok.Data;

@Data
public class CheckoutRequest {
    private String bookingId;
    private String paymentMethod;
}