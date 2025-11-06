package com.btlcnpm.BookingService.controller;

import com.btlcnpm.BookingService.dto.CreateBookingRequest;
import com.btlcnpm.BookingService.model.Booking;
import com.btlcnpm.BookingService.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    /**
     * API tạo đơn đặt vé (trạng thái "PENDING")
     * Đã sửa: Trả về kết quả và bắt lỗi
     */
    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(@RequestBody CreateBookingRequest request) {

        String userId;
        try {
            // Lấy userId thật từ token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            userId = authentication.getPrincipal().toString();
            if (userId == null || userId.equals("anonymousUser")) {
                // Sửa: Trả về Map cho nhất quán
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Yêu cầu token xác thực."));
            }
        } catch (Exception e) {
            // Sửa: Trả về Map cho nhất quán
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token không hợp lệ hoặc thiếu."));
        }

        try {
            // Gọi service với userId thật
            Booking newBooking = bookingService.createBooking(userId, request);

            // SỬA LỖI 1: Trả về newBooking với mã 201 (CREATED)
            return ResponseEntity.status(HttpStatus.CREATED).body(newBooking);

        } catch (Exception e){
            // SỬA LỖI 2: Bắt lỗi từ service (ví dụ: "Không đủ vé")
            // và trả về mã 400 (BAD_REQUEST)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
        // SỬA LỖI 3: Xóa "return null;"
    }


    /**
     * API lấy lịch sử đặt vé
     * Đã sửa: Lấy userId từ token thay vì @RequestHeader
     */
    @GetMapping("/my-history")
    public ResponseEntity<?> getMyBookingHistory(
            @RequestParam(required = false) String status) { // Tham số ?status=SUCCESSFUL (tùy chọn)

        String userId;
        try {
            // Lấy userId thật từ token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            userId = authentication.getPrincipal().toString();
            if (userId == null || userId.equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Yêu cầu token xác thực."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token không hợp lệ hoặc thiếu."));
        }

        try {
            // Gọi service với userId thật
            List<Booking> history = bookingService.getMyBookingHistory(userId, status);
            return ResponseEntity.ok(history); // Trả về danh sách

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }


    /**
     * Từ Ticket service gọi đến (Không thay đổi)
     */
    @GetMapping("/internal/{bookingId}")
    public ResponseEntity<?> getBookingDetails(@PathVariable String bookingId) {
        try {
            Booking booking = bookingService.getBookingById(bookingId);
            if (booking != null) {
                return ResponseEntity.ok(booking);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy booking");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}