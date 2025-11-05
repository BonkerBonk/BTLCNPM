package com.btlcnpm.BookingService.controller;

import com.btlcnpm.BookingService.dto.CreateBookingRequest;
import com.btlcnpm.BookingService.model.Booking;
import com.btlcnpm.BookingService.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;


//     API tạo đơn đặt vé (trạng thái "PENDING")

    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(@RequestBody CreateBookingRequest request) {

        String userId = "temp_user_id_123"; // Tạm thời

        try {
            // Gọi logic  Service
            Booking newBooking = bookingService.createBooking(userId, request);

            // Trả về đối tượng booking đã tạo
            return ResponseEntity.status(HttpStatus.CREATED).body(newBooking);

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


//     API lấy lịch sử đặt vé

    @GetMapping("/my-history")
    public ResponseEntity<?> getMyBookingHistory(
            // Đọc userId từ Header (do Gateway gửi) hoặc dùng giá trị tạm
            @RequestHeader(value = "X-User-Id", defaultValue = "temp_user_id_123") String userId,
            @RequestParam(required = false) String status) { // Tham số ?status=SUCCESSFUL (tùy chọn)

        try {

            List<Booking> history = bookingService.getMyBookingHistory(userId, status);
            return ResponseEntity.ok(history); // Trả về danh sách

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }


//    Từ Ticket service gọi đến
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