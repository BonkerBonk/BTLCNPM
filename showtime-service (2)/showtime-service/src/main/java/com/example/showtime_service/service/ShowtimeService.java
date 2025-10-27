package com.example.showtime_service.service;

import com.example.showtime_service.dto.ShowtimeDto;
import com.example.showtime_service.model.Showtime;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ShowtimeService {
    Showtime createShowtime(ShowtimeDto dto) throws ExecutionException, InterruptedException;
    Showtime getShowtimeById(String showtimeId) throws ExecutionException, InterruptedException;
    List<Showtime> getAllShowtimes() throws ExecutionException, InterruptedException;
}
