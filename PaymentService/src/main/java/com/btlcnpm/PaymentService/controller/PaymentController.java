// PaymentService/src/main/java/com/btlcnpm/PaymentService/controller/PaymentController.java
package com.btlcnpm.PaymentService.controller;

import com.btlcnpm.PaymentService.config.VnpayConfig; // <<< THÊM
import com.btlcnpm.PaymentService.dto.CheckoutRequest;
import com.btlcnpm.PaymentService.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest; // <<< THÊM
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * Sửa:
     * 1. Tiêm HttpServletRequest để lấy IP
     * 2. Gọi hàm service mới
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request, HttpServletRequest httpServletRequest) { // <<< THÊM
        try {
            // Lấy IP từ request
            String ipAddress = VnpayConfig.getIpAddress(httpServletRequest);

            // Gọi service với IP
            Map<String, Object> response = paymentService.processPayment(request, ipAddress);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/momo-ipn")
    public ResponseEntity<?> handleMomoIpn(@RequestBody Map<String, Object> ipnPayload) {
        // (Logic MoMo)
        System.out.println("Nhận được IPN từ MoMo: " + ipnPayload.toString());
        return ResponseEntity.noContent().build();
    }

    /**
     * API MỚI: Để VNPay gọi vào (IPN)
     * Dùng @GetMapping và @RequestParam
     */
    @GetMapping("/vnpay-ipn")
    public ResponseEntity<?> handleVnpayIpn(@RequestParam Map<String, String> ipnPayload) {
        try {
            System.out.println("Nhận được IPN từ VNPay: " + ipnPayload.toString());

            // Gọi service để xử lý (xác thực và cập nhật)
            paymentService.handleVnpayIpn(ipnPayload);

            // Nếu service không ném lỗi -> Thành công
            // Trả về mã 00 cho VNPay
            return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));

        } catch (Exception e) {
            // Nếu service ném lỗi (sai chữ ký, đơn hàng đã xử lý, v.v.)
            System.err.println("Lỗi IPN VNPay: " + e.getMessage());
            // Trả về mã lỗi cho VNPay
            return ResponseEntity.ok(Map.of("RspCode", "97", "Message", e.getMessage()));
        }
    }
}