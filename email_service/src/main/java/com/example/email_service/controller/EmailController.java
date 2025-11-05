package com.example.email_service.controller; // Sửa package nếu cần

import com.example.email_service.dto.ResendTicketRequest;
import com.example.email_service.dto.SendResetLinkRequest; // MỚI
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
@RequestMapping("/api/v1/notification")
public class EmailController {

    @Autowired
    private EmailService emailService;

    // --- API GỬI LẠI VÉ (Giữ nguyên) ---
    @PostMapping("/resend-ticket")
    public ResponseEntity<?> resendTicket(@RequestBody ResendTicketRequest request) {

        try {
            String bookingId = request.getBookingId();
            if (bookingId == null || bookingId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "bookingId là bắt buộc."));
            }

            emailService.resendTicket(bookingId);

            return ResponseEntity.ok(Map.of("message", "Email vé đã được gửi lại."));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi server khi gửi mail: " + e.getMessage()));
        }
    }

    // --- API GỬI LINK RESET (MỚI - Dùng nội bộ) ---
    @PostMapping("/send-reset-link")
    public ResponseEntity<?> sendResetLink(@RequestBody SendResetLinkRequest request) {

        try {
            // API này chỉ nhận email và link rồi gửi đi
            emailService.sendPasswordResetEmail(request.getEmail(), request.getLink());

            return ResponseEntity.ok(Map.of("message", "Email reset đã được gửi."));

        } catch (Exception e) {
            // Bắt các lỗi (ví dụ: Lỗi Gmail)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi server khi gửi mail reset: " + e.getMessage()));
        }
    }
}