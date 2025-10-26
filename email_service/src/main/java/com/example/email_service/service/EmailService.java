package com.example.email_service.service;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender; // Tiêm "người gửi mail"

    @Autowired
    private Firestore firestore; // Tiêm Firestore

    /**
     * Lấy thông tin và gửi lại vé qua email
     */
    public void resendTicket(String bookingId) 
            throws ExecutionException, InterruptedException, RuntimeException {
        
        // 1. Lấy thông tin Đơn hàng (Booking)
        DocumentSnapshot bookingDoc = firestore.collection("bookings").document(bookingId).get().get();
        if (!bookingDoc.exists()) {
            throw new RuntimeException("Không tìm thấy đơn hàng (booking) với ID: " + bookingId);
        }
        String userId = bookingDoc.getString("userId");// Lấy userId [cite: 153]

        // 2. Lấy thông tin Người dùng (User) để biết email
        DocumentSnapshot userDoc = firestore.collection("users").document(userId).get().get();
        if (!userDoc.exists()) {
            throw new RuntimeException("Không tìm thấy người dùng (user) với ID: " + userId);
        }
        String userEmail = userDoc.getString("email");// Lấy email [cite: 98]

        // 3. Lấy thông tin Vé (Ticket) để biết nội dung
        // (Chúng ta giả định TicketService đã tạo vé thành công)
        // [Cách 1: Lấy từ 'tickets' (nếu bạn đã có collection này)]
        // DocumentSnapshot ticketDoc = firestore.collection("tickets").whereEqualTo("bookingId", bookingId).get().get().getDocuments().get(0);
        // String movieTitle = ticketDoc.getString("movieTitle");
        // String theaterName = ticketDoc.getString("theaterName");
        // String showtime = ticketDoc.getTimestamp("showtimeStartTime").toString();

        // [Cách 2: Tự tổng hợp (nếu chưa có 'tickets')]
        // (Để đơn giản, chúng ta sẽ chỉ gửi thông tin cơ bản)
        String movieTitle = "Tên Phim (Lấy từ MovieService)";
        String theaterName = "Tên Rạp (Lấy từ TheaterService)";
        String showtime = "Giờ chiếu (Lấy từ ShowtimeService)";

        // 4. Tạo nội dung Email
        String subject = "Vé xem phim của bạn cho đơn hàng #" + bookingId;
        String emailBody = String.format(
            "Chào bạn,\n\nĐây là vé điện tử của bạn:\n" +
            "- Đơn hàng: %s\n" +
            "- Phim: %s\n" +
            "- Rạp: %s\n" +
            "- Giờ chiếu: %s\n\n" +
            "Cảm ơn bạn đã đặt vé!",
            bookingId, movieTitle, theaterName, showtime
        );

        // 5. Gửi Email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("your-email@gmail.com"); // <-- SỬA LẠI: Email CỦA BẠN (phải khớp với application.properties)
        message.setTo(userEmail); // Gửi đến email của người dùng
        message.setSubject(subject);
        message.setText(emailBody);
        
        javaMailSender.send(message); // Gửi
    }
}