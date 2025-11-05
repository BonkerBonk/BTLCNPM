package com.example.showtime_service.dto;

import lombok.Data;

@Data
public class ShowtimeDto {
    private String movieId;
    private String theaterId;
    private String roomId;
    private String startTime; // ISO format
    private double ticketPrice;
}
