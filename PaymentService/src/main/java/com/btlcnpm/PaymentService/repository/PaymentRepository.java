package com.btlcnpm.PaymentService.repository;

import com.btlcnpm.PaymentService.model.Booking; // Model bạn vừa copy
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class PaymentRepository {

    @Autowired
    private Firestore firestore;

    private static final String BOOKING_COLLECTION = "bookings";


    //Lấy một document booking bằng ID

    public Booking getBookingById(String bookingId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(BOOKING_COLLECTION).document(bookingId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            return document.toObject(Booking.class);
        } else {
            return null;
        }
    }


     //Cập nhật trạng thái của một booking

    public void updateBookingStatus(String bookingId, String newStatus) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(BOOKING_COLLECTION).document(bookingId);

        // Chỉ cập nhật trường "status"
        ApiFuture<WriteResult> future = docRef.update("status", newStatus);

        future.get(); // Chờ cập nhật xong
    }
}