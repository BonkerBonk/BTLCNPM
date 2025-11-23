package com.btlcnpm.TicketService.controller;

import com.btlcnpm.TicketService.dto.TriggerTicketRequest;
import com.btlcnpm.TicketService.model.Ticket;
import com.btlcnpm.TicketService.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ticket")
public class TicketController {

    @Autowired
    private TicketService ticketService;

//     PaymentService gọi đến
//     Kích hoạt tạo vé sau khi thanh toán thành công

    @PostMapping("/internal/create")
    public ResponseEntity<?> createTickets(@RequestBody TriggerTicketRequest request) {
        try {
            ticketService.createTicketsForBooking(request);
            return ResponseEntity.ok(Map.of("message", "Tạo vé thành công.")); // ✅ Trả về JSON
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi khi tạo vé: " + e.getMessage())); // ✅ Trả về JSON
        }
    }

//    App Android gọi đến
//    Lấy vé của tôi

    @GetMapping("/my-tickets")
    public ResponseEntity<?> getMyTickets() {

        String userId;
        try {
            // 1. Lấy thông tin xác thực từ security context (đã được filter xử lý)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            userId = authentication.getPrincipal().toString();

            if (userId == null || userId.equals("anonymousUser")) {
                return ResponseEntity.status(401).body("Yêu cầu token xác thực.");
            }

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token không hợp lệ hoặc thiếu.");
        }

        // 2. userId đã là userId thật (ví dụ: "user_001"), không còn là "temp_user_id_123"
        try {
            List<Ticket> tickets = ticketService.getMyTickets(userId);
            return ResponseEntity.ok(tickets);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi lấy vé: " + e.getMessage());
        }
    }
}