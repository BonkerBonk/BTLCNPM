package com.example.showtime_service.repository;

import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

@Repository
public class ShowtimeRepository {
    private final Firestore firestore;

    public ShowtimeRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Firestore getFirestore() {
        return firestore;
    }
}
