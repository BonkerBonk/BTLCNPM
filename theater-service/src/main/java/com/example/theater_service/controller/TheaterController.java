package com.example.theater_service.controller;

import com.example.theater_service.dto.TheaterDto;
import com.example.theater_service.model.GeoLocation;
import com.example.theater_service.model.Theater;
import com.example.theater_service.service.TheaterService;
import com.google.cloud.firestore.GeoPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/theaters")
@RequiredArgsConstructor
public class TheaterController {

    private final TheaterService service;

    @PostMapping
    public Theater create(@RequestBody TheaterDto dto) throws ExecutionException, InterruptedException {
        Theater theater = new Theater(
                dto.getTheaterId(),
                dto.getName(),
                dto.getAddress(),
                dto.getCity(),
                new GeoPoint(dto.getLatitude(), dto.getLongitude())
        );
        return service.createTheater(theater);
    }

    @GetMapping("/{id}")
    public Theater getById(@PathVariable String id) throws ExecutionException, InterruptedException {
        return service.getTheaterById(id);
    }

    @GetMapping
    public List<Theater> getAll() throws ExecutionException, InterruptedException {
        return service.getAllTheaters();
    }

    @GetMapping("/city/{city}")
    public List<Theater> getByCity(@PathVariable String city) throws ExecutionException, InterruptedException {
        return service.getByCity(city);
    }
}