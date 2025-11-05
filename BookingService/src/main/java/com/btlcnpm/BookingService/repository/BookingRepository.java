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
        // Tự động tạo một ID mới cho document
        String bookingId = getBookingCollection().document().getId();
        booking.setBookingId(bookingId); // Gán ID này vào đối tượng

        // Lưu đối tượng vào document có ID vừa tạo
        ApiFuture<WriteResult> future = getBookingCollection().document(bookingId).set(booking);

        // Chờ cho đến khi lưu xong
        future.get();

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
        // Bắt đầu câu truy vấn : WHERE userId == [userId]
        Query query = getBookingCollection().whereEqualTo("userId", userId);

        // Nếu người dùng có cung cấp 'status' (PENDING, SUCCESSFUL, FAILED)
        //    thì thêm điều kiện đó vào câu truy vấn
        if (status != null && !status.isEmpty()) {
            query = query.whereEqualTo("status", status);
        }

        // (Trong một dự án thật, bạn nên thêm .orderBy("createdAt", Query.Direction.DESCENDING)
        // để sắp xếp mới nhất lên đầu, nhưng nó yêu cầu tạo index trên Firebase)

        // Thực thi truy vấn
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        //  aChuyển kết quả thành danh sách (List)
        List<Booking> bookings = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
            bookings.add(document.toObject(Booking.class));
        }

        return bookings;
    }
}