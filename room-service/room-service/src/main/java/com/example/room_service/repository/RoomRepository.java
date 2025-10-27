package com.example.room_service.repository;

import com.example.room_service.model.Room;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class RoomRepository {
    private final Firestore db;

    public RoomRepository(Firestore db) {
        this.db = db;
    }

    public Room save(Room room) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> future = db.collection("rooms")
                .document(room.getRoomId())
                .set(room);
        future.get();
        return room;
    }

    public List<Room> findAllByTheaterId(String theaterId) throws ExecutionException, InterruptedException {
        CollectionReference roomsRef = db.collection("rooms");
        Query query = roomsRef.whereEqualTo("theaterId", theaterId);
        ApiFuture<QuerySnapshot> future = query.get();

        List<Room> rooms = new ArrayList<>();
        for (DocumentSnapshot doc : future.get().getDocuments()) {
            Room room = doc.toObject(Room.class);
            room.setRoomId(doc.getId());
            rooms.add(room);
        }
        return rooms;
    }
}
