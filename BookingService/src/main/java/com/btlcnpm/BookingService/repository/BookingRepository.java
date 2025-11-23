package com.btlcnpm.BookingService.repository;

import com.btlcnpm.BookingService.model.Booking;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository // Đánh dấu đây là lớp Repository
public class BookingRepository {

    @Autowired
    private Firestore firestore; // Tiêm Bean Firestore đã tạo ở FirebaseConfig

    // Tên collection trên Firestore [cite: 149]
    private static final String COLLECTION_NAME = "bookings";

    private CollectionReference getBookingCollection() {
        return firestore.collection(COLLECTION_NAME);
    }

    /**
     * Lưu một đối tượng Booking mới vào Firestore
     */
    public Booking save(Booking booking) throws Exception {
        // Nếu booking chưa có ID -> Tạo mới
        if (booking.getBookingId() == null || booking.getBookingId().isEmpty()) {
            String bookingId = getBookingCollection().document().getId();
            booking.setBookingId(bookingId);
        }

        // Lưu hoặc cập nhật (Firestore sẽ overwrite nếu document đã tồn tại)
        ApiFuture<WriteResult> future = getBookingCollection()
                .document(booking.getBookingId())
                .set(booking);

        future.get();

        System.out.println("✅ Đã lưu/cập nhật booking: " + booking.getBookingId() + " với status: " + booking.getStatus());

        return booking;
    }


    public Booking getBookingById(String bookingId) throws Exception {
        DocumentReference docRef = getBookingCollection().document(bookingId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return document.toObject(Booking.class);
        }
        return null;
    }

    public List<Booking> getBookingsByUserIdAndStatus(String userId, String status) throws ExecutionException, InterruptedException {
        Query query = getBookingCollection().whereEqualTo("userId", userId);

        if (status != null && !status.isEmpty()) {
            query = query.whereEqualTo("status", status);
        }

        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        List<Booking> bookings = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
            bookings.add(document.toObject(Booking.class));
        }

        return bookings;
    }

    public boolean updateStatus(String bookingId, String newStatus) throws Exception {
        DocumentReference docRef = getBookingCollection().document(bookingId);

        ApiFuture<WriteResult> future = docRef.update("status", newStatus);
        future.get();

        System.out.println("✅ Đã cập nhật status của booking " + bookingId + " thành " + newStatus);

        return true;
    }
}