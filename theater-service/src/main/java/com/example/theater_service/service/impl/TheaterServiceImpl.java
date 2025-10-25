package com.example.theater_service.service.impl;

import com.example.theater_service.model.Theater;
import com.example.theater_service.service.TheaterService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class TheaterServiceImpl implements TheaterService {

    private static final String COLLECTION_NAME = "theaters";

    @Override
    public Theater createTheater(Theater theater) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference theatersCollection = db.collection(COLLECTION_NAME);
        DocumentReference docRef;

        if (theater.getTheaterId() == null || theater.getTheaterId().isBlank()) {
            ApiFuture<QuerySnapshot> future = theatersCollection.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            int maxIndex = 0;
            for (QueryDocumentSnapshot doc : documents) {
                String id = doc.getId();
                if (id != null && id.startsWith("TH")) {
                    try {
                        int number = Integer.parseInt(id.substring(2));
                        if (number > maxIndex) {
                            maxIndex = number;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }

            // Sinh ID mới
            String newId = "TH" + (maxIndex + 1);
            theater.setTheaterId(newId); // Set field
            docRef = theatersCollection.document(newId); // Set as document ID
        } else {

            docRef = theatersCollection.document(theater.getTheaterId());
            theater.setTheaterId(theater.getTheaterId()); // Bảo đảm lưu cả field
        }

        ApiFuture<WriteResult> result = docRef.set(theater);
        log.info("🎬 Theater created with ID: {}, at: {}", theater.getTheaterId(), result.get().getUpdateTime());
        return theater;
    }

    @Override
    public Theater getTheaterById(String id) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(id);
        DocumentSnapshot snapshot = docRef.get().get();

        if (snapshot.exists()) {
            Theater theater = snapshot.toObject(Theater.class);
            if (theater != null && theater.getTheaterId() == null) {
                theater.setTheaterId(snapshot.getId()); // fallback nếu thiếu
            }
            return theater;
        }
        return null;
    }

    @Override
    public List<Theater> getAllTheaters() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Theater> theaters = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Theater theater = doc.toObject(Theater.class);
            if (theater != null && theater.getTheaterId() == null) {
                theater.setTheaterId(doc.getId()); // fallback nếu thiếu
            }
            theaters.add(theater);
        }
        return theaters;
    }

    @Override
    public List<Theater> getByCity(String city) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("city", city)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Theater> theaters = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Theater theater = doc.toObject(Theater.class);
            if (theater != null && theater.getTheaterId() == null) {
                theater.setTheaterId(doc.getId());
            }
            theaters.add(theater);
        }
        return theaters;
    }
}
