package com.example.room_service.controller;

import com.example.room_service.dto.RoomDto;
import com.example.room_service.model.Room;
import com.example.room_service.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/theater/theaters")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     *  POST /api/v1/theater/theaters/{theaterId}/rooms
     *  vd : http://localhost:8088/api/v1/theater/theaters/theater123/rooms
     */
    @PostMapping("/{theaterId}/rooms")
    public ResponseEntity<Room> createRoom(@PathVariable String theaterId,
                                           @RequestBody RoomDto dto) throws ExecutionException, InterruptedException {
        Room created = roomService.createRoom(theaterId, dto);
        return ResponseEntity.status(201).body(created);
    }

    /**
     *  GET /api/v1/theater/theaters/{theaterId}/rooms
     *  vd : http://localhost:8088/api/v1/theater/theaters/theater123/rooms
     */
    @GetMapping("/{theaterId}/rooms")
    public ResponseEntity<List<Room>> getRoomsByTheaterId(@PathVariable String theaterId)
            throws ExecutionException, InterruptedException {
        List<Room> rooms = roomService.getAllRooms(theaterId);
        return ResponseEntity.ok(rooms);
    }
}
