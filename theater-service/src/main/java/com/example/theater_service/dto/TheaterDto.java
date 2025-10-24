package com.example.theater_service.dto;

import com.example.theater_service.model.GeoLocation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheaterDto {
    private String theaterId;
    private String name;
    private String address;
    private String city;
    private double latitude;
    private double longitude;
}
