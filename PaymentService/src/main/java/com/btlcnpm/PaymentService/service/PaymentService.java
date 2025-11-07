// PaymentService/src/main/java/com/btlcnpm/PaymentService/service/PaymentService.java
package com.btlcnpm.PaymentService.service;

import com.btlcnpm.PaymentService.dto.CheckoutRequest;
import java.util.Map; // <<< THÊM

public interface PaymentService {

    // Hàm này sẽ bị thay thế
    String processPayment(CheckoutRequest request) throws Exception;

    // Hàm MỚI để tạo thanh toán (lấy IP từ Controller)
    Map<String, Object> processPayment(CheckoutRequest request, String ipAddress) throws Exception;

    // Hàm MỚI để xử lý IPN (lấy payload từ Controller)
    void handleVnpayIpn(Map<String, String> ipnPayload) throws Exception;
}