package com.example.room_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    private String roomId;
    private String name;
    private int capacity;
    private String theaterId;
}
