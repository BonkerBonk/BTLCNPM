package com.example.room_service.service;

import com.example.room_service.dto.RoomDto;
import com.example.room_service.model.Room;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface RoomService {
    Room createRoom(String theaterId, RoomDto dto) throws ExecutionException, InterruptedException;
    List<Room> getAllRooms(String theaterId) throws ExecutionException, InterruptedException;

    Room getRoomById(String roomId) throws ExecutionException, InterruptedException;
}
