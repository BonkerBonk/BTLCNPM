package com.btlcnpm.CheckinService.controller;

import com.btlcnpm.CheckinService.dto.ScanRequest;
import com.btlcnpm.CheckinService.model.Ticket;
import com.btlcnpm.CheckinService.service.CheckinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/checkin")
public class CheckinController {

    @Autowired
    private CheckinService checkinService;


    @PostMapping("/scan")
    public ResponseEntity<?> scanTicket(@RequestBody ScanRequest request) {

        try {
            // Gọi logic nghiệp vụ
            Ticket updatedTicket = checkinService.processCheckin(request.getQrCodeData());

            // Trả về response thành công theo đề cương
            Map<String, String> response = Map.of(
                    "message", "Check-in thành công",
                    "ticketStatus", updatedTicket.getStatus() // Sẽ là "USED"
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }
}