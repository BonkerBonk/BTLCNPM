package com.btlcnpm.PaymentService.model;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    private String bookingId;
    private String userId;
    private String showtimeId;
    private int quantity;
    private double totalAmount;
    private String status;           //(PENDING, SUCCESSFUL, FAILED)
    private Timestamp createdAt;
    // private Timestamp expiresAt; [cite_start]//
}