package com.example.email_service.controller;

import com.example.email_service.dto.ResendTicketRequest;
import com.example.email_service.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notification") // Đường dẫn gốc theo hợp đồng API
public class EmailController {

    @Autowired
    private EmailService emailService;

    /**
     * API Gửi lại vé
     */
    @PostMapping("/resend-ticket") // -> /api/v1/notification/resend-ticket
    public ResponseEntity<?> resendTicket(@RequestBody ResendTicketRequest request) {
        
        try {
            // 1. Lấy bookingId từ request
            String bookingId = request.getBookingId();
            if (bookingId == null || bookingId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "bookingId là bắt buộc."));
            }

            // 2. Gọi Service để gửi mail
            emailService.resendTicket(bookingId);

            // 3. Trả về thông báo thành công
            return ResponseEntity.ok(Map.of("message", "Email vé đã được gửi lại."));

        } catch (RuntimeException e) {
            // Bắt lỗi "Không tìm thấy đơn hàng" từ Service
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            // Bắt các lỗi khác (ví dụ: Lỗi Gmail, Lỗi Firestore)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi server khi gửi mail: " + e.getMessage()));
        }
    }
}