package com.example.showtime_service.controller;

import com.example.showtime_service.dto.ShowtimeDto;
import com.example.showtime_service.dto.ShowtimeResponse;
import com.example.showtime_service.dto.UpdateTicketsRequest; // <<< THÊM IMPORT NÀY
import com.example.showtime_service.model.Showtime;
import com.example.showtime_service.service.ShowtimeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map; // <<< THÊM IMPORT NÀY
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/showtime")
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    public ShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }

    @PostMapping
    public ResponseEntity<Showtime> create(@RequestBody ShowtimeDto dto) throws ExecutionException, InterruptedException {
        return ResponseEntity.status(201).body(showtimeService.createShowtime(dto));
    }

    @GetMapping("/showtimes")
    public ResponseEntity<List<ShowtimeResponse>> getAll() throws ExecutionException, InterruptedException {
        List<Showtime> showtimes = showtimeService.getAllShowtimes();
        List<ShowtimeResponse> responseList = new ArrayList<>();

        for (Showtime s : showtimes) {
            ShowtimeResponse res = new ShowtimeResponse();
            res.setShowtimeId(s.getShowtimeId());
            res.setMovieId(s.getMovieId());
            res.setTheaterId(s.getTheaterId());
            res.setRoomId(s.getRoomId());
            res.setStartTime(s.getStartTime().toSqlTimestamp().toInstant().toString()); // ISO
            res.setTicketPrice(s.getTicketPrice());
            res.setTotalTickets(s.getTotalTickets());
            res.setAvailableTickets(s.getAvailableTickets());
            responseList.add(res);
        }

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/showtimes/{showtimeId}")
    public ResponseEntity<ShowtimeResponse> getById(@PathVariable String showtimeId) throws ExecutionException, InterruptedException {
        Showtime showtime = showtimeService.getShowtimeById(showtimeId);

        ShowtimeResponse response = new ShowtimeResponse();
        response.setShowtimeId(showtime.getShowtimeId());
        response.setMovieId(showtime.getMovieId());
        response.setTheaterId(showtime.getTheaterId());
        response.setRoomId(showtime.getRoomId());
        response.setStartTime(showtime.getStartTime().toSqlTimestamp().toInstant().toString()); // ✅ ISO format
        response.setTicketPrice(showtime.getTicketPrice());
        response.setTotalTickets(showtime.getTotalTickets());
        response.setAvailableTickets(showtime.getAvailableTickets());

        return ResponseEntity.ok(response);
    }

    // <<< THÊM API ENDPOINT MỚI MÀ TV5 CẦN >>>
    /**
     * API nội bộ (Internal) để BookingService gọi sang và trừ vé
     */
    @PostMapping("/internal/update-tickets")
    public ResponseEntity<?> updateAvailableTickets(@RequestBody UpdateTicketsRequest request) {
        try {
            showtimeService.updateAvailableTickets(request);
            return ResponseEntity.ok(Map.of("message", "Cập nhật vé thành công."));
        } catch (Exception e) {
            // Trả về lỗi 400 (Bad Request) nếu có lỗi (ví dụ: hết vé)
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }
}