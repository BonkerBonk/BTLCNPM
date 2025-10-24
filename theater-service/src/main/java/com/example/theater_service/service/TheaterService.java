package com.example.theater_service.service;


import com.example.theater_service.model.Theater;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface TheaterService {
    Theater createTheater(Theater theater) throws ExecutionException, InterruptedException;
    Theater getTheaterById(String id) throws ExecutionException, InterruptedException;
    List<Theater> getAllTheaters() throws ExecutionException, InterruptedException;
    List<Theater> getByCity(String city) throws ExecutionException, InterruptedException;
}
