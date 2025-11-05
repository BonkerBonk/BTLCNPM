package com.example.showtime_service.service;

import com.example.showtime_service.dto.ShowtimeDto;
import com.example.showtime_service.dto.UpdateTicketsRequest;
import com.example.showtime_service.model.Showtime;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ShowtimeService {
    Showtime createShowtime(ShowtimeDto dto) throws ExecutionException, InterruptedException;
    Showtime getShowtimeById(String showtimeId) throws ExecutionException, InterruptedException;
    List<Showtime> getAllShowtimes() throws ExecutionException, InterruptedException;

    // <<< THÊM HÀM MỚI NÀY ĐỂ TV5 GỌI >>>
    void updateAvailableTickets(UpdateTicketsRequest request) throws Exception;
}
