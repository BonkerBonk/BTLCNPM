package com.example.showtime_service.service.impl;

import com.example.showtime_service.dto.ShowtimeDto;
import com.example.showtime_service.dto.UpdateTicketsRequest; // <<< THÊM IMPORT NÀY
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
    private final String COLLECTION_NAME = "showtimes"; // <<< Thêm hằng số

    public ShowtimeServiceImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public Showtime createShowtime(ShowtimeDto dto) throws ExecutionException, InterruptedException {
        int roomCapacity = 100; // TODO: Lấy cái này từ RoomService

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

        ApiFuture<WriteResult> future = firestore.collection(COLLECTION_NAME)
                .document(showtime.getShowtimeId())
                .set(showtime);
        future.get();

        return showtime;
    }
    @Override
    public Showtime getShowtimeById(String showtimeId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(COLLECTION_NAME).document(showtimeId).get().get();
        if (!doc.exists()) {
            throw new RuntimeException("Showtime ID not found: " + showtimeId);
        }
        return doc.toObject(Showtime.class);
    }

    @Override
    public List<Showtime> getAllShowtimes() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<Showtime> showtimes = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            showtimes.add(doc.toObject(Showtime.class));
        }
        return showtimes;
    }

    // <<< THÊM HÀM LOGIC MỚI CHO API TRỪ VÉ >>>
    @Override
    @SuppressWarnings("deprecation") // Tắt cảnh báo cho @Transaction
    public void updateAvailableTickets(UpdateTicketsRequest request) throws Exception {
        DocumentReference showtimeRef = firestore.collection(COLLECTION_NAME).document(request.getShowtimeId());

        // Chạy một transaction để đảm bảo an toàn dữ liệu
        ApiFuture<Void> future = firestore.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(showtimeRef).get();

            if (!snapshot.exists()) {
                throw new ExecutionException(new Exception("Suất chiếu không tồn tại: " + request.getShowtimeId()));
            }

            Showtime showtime = snapshot.toObject(Showtime.class);
            int currentTickets = showtime.getAvailableTickets();
            int newAvailableTickets = currentTickets + request.getQuantityToChange(); // (ví dụ: 100 + (-2) = 98)

            if (newAvailableTickets < 0) {
                throw new ExecutionException(new Exception("Không đủ vé. Chỉ còn " + currentTickets + " vé."));
            }

            // Cập nhật lại số vé
            transaction.update(showtimeRef, "availableTickets", newAvailableTickets);
            return null;
        });

        // Chờ transaction hoàn tất
        future.get();
    }


    //  Hàm tự sinh ID tăng dần theo định dạng ST001, ST002...
    private String generateNextShowtimeId() throws ExecutionException, InterruptedException {
        CollectionReference colRef = firestore.collection(COLLECTION_NAME);

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