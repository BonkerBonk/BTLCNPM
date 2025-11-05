package com.btlcnpm.PaymentService.controller;

import com.btlcnpm.PaymentService.dto.CheckoutRequest;
import com.btlcnpm.PaymentService.service.PaymentService; // Quan trọng: Import service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map; // Import Map

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService; // Quan trọng: Tiêm service vào

    /**
     * API xác nhận thanh toán (giả lập)
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request) {

        try {
            // Quan trọng: Gọi logic nghiệp vụ
            String message = paymentService.processPayment(request);

            // Trả về response JSON đúng theo đề cương
            Map<String, String> response = Map.of(
                    "bookingId", request.getBookingId(),
                    "status", "SUCCESSFUL",
                    "message", message
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Trả về lỗi 400 (ví dụ: "Thanh toán thất bại")
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }
}