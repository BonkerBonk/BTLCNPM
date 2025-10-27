package com.example.showtime_service.model;

import com.google.cloud.Timestamp;
import lombok.Data;

@Data
public class Showtime {
    private String showtimeId;
    private String movieId;
    private String theaterId;
    private String roomId;
    private Timestamp startTime; // ✅ Đổi từ String sang Timestamp
    private double ticketPrice;
    private int totalTickets;
    private int availableTickets;
}
