package com.example.room_service.controller;

import com.example.room_service.dto.RoomDto;
import com.example.room_service.model.Room;
import com.example.room_service.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // <<< THÊM IMPORT NÀY
import java.util.concurrent.ExecutionException;

@RestController
// <<< THAY ĐỔI 1: Đổi RequestMapping để khớp với TicketService (TV5) >>>
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // <<< THAY ĐỔI 2: API MỚI MÀ TV5 CẦN >>>
    /**
     * API nội bộ (Internal) để TicketService (TV5) gọi lấy thông tin phòng
     * GET /api/v1/rooms/{roomId}
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<?> getRoomById(@PathVariable String roomId) {
        try {
            Room room = roomService.getRoomById(roomId);
            // Trả về DTO đơn giản mà TicketService cần (chỉ cần name)
            return ResponseEntity.ok(Map.of("name", room.getName()));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }


    // <<< THAY ĐỔI 3: Sửa lại API tạo phòng cho hợp lý (không cần theaterId ở path) >>>
    /**
     * POST /api/v1/rooms
     * (Giả định theaterId đã có trong DTO)
     */
    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody RoomDto dto) throws ExecutionException, InterruptedException {
        // Chúng ta cần theaterId, hãy đảm bảo nó có trong DTO
        if (dto.getTheaterId() == null || dto.getTheaterId().isEmpty()) {
            return ResponseEntity.status(400).body(null); // Cần theaterId
        }
        Room created = roomService.createRoom(dto.getTheaterId(), dto);
        return ResponseEntity.status(201).body(created);
    }

    // <<< THAY ĐỔI 4: Sửa API lấy danh sách phòng theo theaterId (dùng query param) >>>
    /**
     * GET /api/v1/rooms?theaterId=TH_001
     */
    @GetMapping
    public ResponseEntity<List<Room>> getRoomsByTheater(
            @RequestParam String theaterId
    ) throws ExecutionException, InterruptedException {
        List<Room> rooms = roomService.getAllRooms(theaterId);
        return ResponseEntity.ok(rooms);
    }
}