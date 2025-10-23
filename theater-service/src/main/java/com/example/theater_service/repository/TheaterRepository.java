package com.example.theater_service.repository;

import com.example.theater_service.model.Theater;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class TheaterRepository {

    private static final String COLLECTION_NAME = "theaters";

    public Theater save(Theater theater) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME).document(theater.getTheaterId()).set(theater);
        future.get(); // Đợi ghi xong
        return theater;
    }

    public Theater findById(String id) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(id).get().get();
        return snapshot.exists() ? snapshot.toObject(Theater.class) : null;
    }

    public List<Theater> findAll() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        List<QueryDocumentSnapshot> docs = db.collection(COLLECTION_NAME).get().get().getDocuments();
        List<Theater> theaters = new ArrayList<>();
        for (DocumentSnapshot doc : docs) {
            theaters.add(doc.toObject(Theater.class));
        }
        return theaters;
    }

    public List<Theater> findByCity(String city) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        QuerySnapshot querySnapshot = db.collection(COLLECTION_NAME).whereEqualTo("city", city).get().get();
        List<Theater> theaters = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            theaters.add(doc.toObject(Theater.class));
        }
        return theaters;
    }
}