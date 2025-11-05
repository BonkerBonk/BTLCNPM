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
    private static final String COLLECTION_NAME = "rooms"; // <<< THÊM HẰNG SỐ

    public RoomRepository(Firestore db) {
        this.db = db;
    }

    public Room save(Room room) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME) // <<< Sửa "rooms"
                .document(room.getRoomId())
                .set(room);
        future.get();
        return room;
    }

    public List<Room> findAllByTheaterId(String theaterId) throws ExecutionException, InterruptedException {
        CollectionReference roomsRef = db.collection(COLLECTION_NAME); // <<< Sửa "rooms"
        Query query = roomsRef.whereEqualTo("theaterId", theaterId);
        ApiFuture<QuerySnapshot> future = query.get();

        List<Room> rooms = new ArrayList<>();
        for (DocumentSnapshot doc : future.get().getDocuments()) {
            Room room = doc.toObject(Room.class);
            // Gán ID cho chắc chắn, vì có thể bạn không lưu roomId_
            if (room != null) {
                room.setRoomId(doc.getId());
            }
            rooms.add(room);
        }
        return rooms;
    }

    // <<< THÊM HÀM MỚI NÀY >>>
    public Room findById(String roomId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(roomId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return document.toObject(Room.class);
        }
        return null;
    }
}