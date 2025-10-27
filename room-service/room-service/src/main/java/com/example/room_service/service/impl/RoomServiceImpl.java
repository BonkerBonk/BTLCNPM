package com.example.room_service.service.impl;

import com.example.room_service.dto.RoomDto;
import com.example.room_service.model.Room;
import com.example.room_service.repository.RoomRepository;
import com.example.room_service.service.RoomService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public Room createRoom(String theaterId, RoomDto dto) throws ExecutionException, InterruptedException {
        Room room = new Room();
        room.setRoomId(UUID.randomUUID().toString());
        room.setName(dto.getName());
        room.setCapacity(dto.getCapacity());
        room.setTheaterId(theaterId);

        return roomRepository.save(room);
    }

    @Override
    public List<Room> getAllRooms(String theaterId) throws ExecutionException, InterruptedException {
        return roomRepository.findAllByTheaterId(theaterId);
    }
}
