package com.example.showtime_service.service.impl;

import com.example.showtime_service.dto.ShowtimeDto;
import com.example.showtime_service.model.Showtime;
import com.example.showtime_service.service.ShowtimeService;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ShowtimeServiceImpl implements ShowtimeService {

    private final Firestore firestore;

    public ShowtimeServiceImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public Showtime createShowtime(ShowtimeDto dto) throws ExecutionException, InterruptedException {
        int roomCapacity = 100;

        Showtime showtime = new Showtime();
        showtime.setShowtimeId(generateNextShowtimeId()); //  Tự sinh ID ST001, ST002, ...
        showtime.setMovieId(dto.getMovieId());
        showtime.setTheaterId(dto.getTheaterId());
        showtime.setRoomId(dto.getRoomId());

        Instant instant = Instant.parse(dto.getStartTime());
        showtime.setStartTime(Timestamp.ofTimeSecondsAndNanos(instant.getEpochSecond(), 0));

        showtime.setTicketPrice(dto.getTicketPrice());
        showtime.setTotalTickets(roomCapacity);
        showtime.setAvailableTickets(roomCapacity);

        ApiFuture<WriteResult> future = firestore.collection("showtimes")
                .document(showtime.getShowtimeId())
                .set(showtime);
        future.get();

        return showtime;
    }
    @Override
    public Showtime getShowtimeById(String showtimeId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection("showtimes").document(showtimeId).get().get();
        if (!doc.exists()) {
            throw new RuntimeException("Showtime ID not found: " + showtimeId);
        }
        return doc.toObject(Showtime.class);
    }

    @Override
    public List<Showtime> getAllShowtimes() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection("showtimes").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<Showtime> showtimes = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            showtimes.add(doc.toObject(Showtime.class));
        }
        return showtimes;
    }

    //  Hàm tự sinh ID tăng dần theo định dạng ST001, ST002...
    private String generateNextShowtimeId() throws ExecutionException, InterruptedException {
        CollectionReference colRef = firestore.collection("showtimes");

        ApiFuture<QuerySnapshot> future = colRef.select("showtimeId").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        int max = 0;
        for (QueryDocumentSnapshot doc : documents) {
            String id = doc.getString("showtimeId");
            if (id != null && id.startsWith("ST")) {
                try {
                    int num = Integer.parseInt(id.substring(2));
                    if (num > max) {
                        max = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        int nextId = max + 1;
        return String.format("ST%03d", nextId); // Ex: ST001, ST002,...
    }
}
