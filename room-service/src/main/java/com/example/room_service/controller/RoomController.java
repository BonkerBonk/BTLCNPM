package com.example.room_service.controller;

import com.example.room_service.dto.RoomDto;
import com.example.room_service.model.Room;
import com.example.room_service.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * API nội bộ (Internal) để TicketService (TV5) gọi lấy thông tin phòng
     * GET /api/v1/rooms/{roomId}
     *
     * ===== SỬA: Trả về đầy đủ thông tin Room thay vì chỉ name =====
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<?> getRoomById(@PathVariable String roomId) {
        try {
            Room room = roomService.getRoomById(roomId);
            // Trả về đầy đủ thông tin (bao gồm cả capacity nếu cần)
            return ResponseEntity.ok(Map.of(
                    "roomId", room.getRoomId(),
                    "name", room.getName(),
                    "capacity", room.getCapacity(),
                    "theaterId", room.getTheaterId()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * POST /api/v1/rooms
     * Tạo phòng mới
     */
    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody RoomDto dto) throws ExecutionException, InterruptedException {
        if (dto.getTheaterId() == null || dto.getTheaterId().isEmpty()) {
            return ResponseEntity.status(400).body(null);
        }
        Room created = roomService.createRoom(dto.getTheaterId(), dto);
        return ResponseEntity.status(201).body(created);
    }

    /**
     * GET /api/v1/rooms?theaterId=TH_001
     * Lấy danh sách phòng theo rạp
     */
    @GetMapping
    public ResponseEntity<List<Room>> getRoomsByTheater(
            @RequestParam String theaterId
    ) throws ExecutionException, InterruptedException {
        List<Room> rooms = roomService.getAllRooms(theaterId);
        return ResponseEntity.ok(rooms);
    }
}