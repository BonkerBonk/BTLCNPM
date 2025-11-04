package com.btlcnpm.TicketService.controller;

import com.btlcnpm.TicketService.dto.TriggerTicketRequest;
import com.btlcnpm.TicketService.model.Ticket;
import com.btlcnpm.TicketService.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            return ResponseEntity.ok("Tạo vé thành công.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi tạo vé: " + e.getMessage());
        }
    }

//    App Android gọi đến
//    Lấy vé của tôi

    @GetMapping("/my-tickets")
    public ResponseEntity<?> getMyTickets() {
        // TODO: Lấy userId từ Token
        String userId = "temp_user_id_123";

        try {
            // Gọi hàm service
            List<Ticket> tickets = ticketService.getMyTickets(userId);

            // Trả về danh sách vé
            return ResponseEntity.ok(tickets);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi lấy vé: " + e.getMessage());
        }
    }
}