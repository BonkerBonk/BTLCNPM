package com.example.showtime_service.dto;

import lombok.Data;

@Data
public class ShowtimeResponse {
    private String showtimeId;
    private String movieId;
    private String theaterId;
    private String roomId;
    private String startTime; // ISO-8601 string
    private double ticketPrice;
    private int totalTickets;
    private int availableTickets;
}
