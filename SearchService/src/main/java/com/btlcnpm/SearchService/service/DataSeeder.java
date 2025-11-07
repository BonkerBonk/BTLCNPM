package com.btlcnpm.SearchService.service; // (Giữ nguyên package của bạn)

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.GeoPoint;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DataSeeder {

    private final Firestore db;

    public DataSeeder(Firestore firestore) {
        this.db = firestore;
    }

    // (QUAN TRỌNG) Đảm bảo dòng @PostConstruct này KHÔNG bị comment
    @PostConstruct
    public void seedDatabase() {
        System.out.println("--- BẮT ĐẦU THÊM DỮ LIỆU MẪU (SEEDING) ---");
        try {
            // Xóa dữ liệu cũ trước khi thêm (để tránh trùng lặp)
            // (Trong dự án thực tế không nên làm vậy, nhưng để test thì OK)
            System.out.println("... Xóa dữ liệu cũ ...");
            db.collection("users").listDocuments().forEach(doc -> doc.delete());
            db.collection("movies").listDocuments().forEach(doc -> doc.delete());
            db.collection("theaters").listDocuments().forEach(doc -> doc.delete()); // Sẽ xóa cả subcollection rooms
            db.collection("showtimes").listDocuments().forEach(doc -> doc.delete());
            db.collection("bookings").listDocuments().forEach(doc -> doc.delete());
            db.collection("tickets").listDocuments().forEach(doc -> doc.delete());
            db.collection("reviews").listDocuments().forEach(doc -> doc.delete());
            db.collection("device_tokens").listDocuments().forEach(doc -> doc.delete());

            // Thêm dữ liệu theo thứ tự
            seedUsers();
            seedMovies();
            seedTheatersAndRooms(); // Phải chạy trước showtimes
            seedShowtimes(); // *** DỮ LIỆU LIÊN KẾT CHÍNH NẰM Ở ĐÂY ***
            seedBookings(); // Phải chạy trước tickets
            seedTickets();
            seedReviews();
            seedDeviceTokens();
            System.out.println("--- THÊM DỮ LIỆU MẪU THÀNH CÔNG ---");
        } catch (Exception e) {
            System.err.println("!!! LỖI KHI THÊM DỮ LIỆU MẪU: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Hàm hỗ trợ: Chuyển đổi chuỗi ngày ISO thành đối tượng Timestamp
    private Timestamp ts(String isoString) {
        return Timestamp.parseTimestamp(isoString);
    }

    // 1. Thêm Người dùng (users)
    private void seedUsers() throws Exception {
        List<Map<String, Object>> users = List.of(
                Map.of("userId", "user_001", "email", "nguyenvana@gmail.com", "fullName", "Nguyễn Văn A", "phoneNumber", "0901234567", "profileImageUrl", "https://picsum.photos/seed/user_001/200", "dateOfBirth", ts("1995-10-20T00:00:00Z"), "createdAt", ts("2025-01-15T09:30:00Z")),
                Map.of("userId", "user_002", "email", "tranvanb@gmail.com", "fullName", "Trần Văn B", "phoneNumber", "0902345678", "profileImageUrl", "https://picsum.photos/seed/user_002/200", "dateOfBirth", ts("1998-05-12T00:00:00Z"), "createdAt", ts("2025-01-16T10:00:00Z"))
                // (Thêm các user khác nếu cần)
        );
        System.out.println("... Seeding users ...");
        for (Map<String, Object> user : users) {
            db.collection("users").document((String) user.get("userId")).set(user).get();
        }
    }

    // 2. Thêm Phim (movies)
    private void seedMovies() throws Exception {
        List<Map<String, Object>> movies = List.of(
                Map.ofEntries(
                        Map.entry("movieId", "m_001"), Map.entry("title", "Lật Mặt 7: Một Điều Ước"), Map.entry("description", "Phim mới nhất của Lý Hải, kể về câu chuyện cảm động của bà Hai và 5 người con."), Map.entry("posterUrl", "https://picsum.photos/seed/latmat7/200/300"), Map.entry("backdropUrl", "https://picsum.photos/seed/latmat7_bg/400/200"), Map.entry("trailerUrl", "https://www.youtube.com/watch?v=..."), Map.entry("durationMinutes", 135), Map.entry("genre", List.of("Gia đình", "Chính kịch")), Map.entry("releaseDate", ts("2025-04-26T00:00:00Z")), Map.entry("director", "Lý Hải"), Map.entry("cast", List.of("Thanh Hiền", "Trương Minh Cường"))
                ),
                Map.ofEntries(
                        Map.entry("movieId", "m_002"), Map.entry("title", "Dune: Hành Tinh Cát 2"), Map.entry("description", "Hành trình của Paul Atreides báo thù những kẻ đã hủy hoại gia đình mình."), Map.entry("posterUrl", "https://picsum.photos/seed/dune2/200/300"), Map.entry("backdropUrl", "https://picsum.photos/seed/dune2_bg/400/200"), Map.entry("trailerUrl", "https://www.youtube.com/watch?v=..."), Map.entry("durationMinutes", 166), Map.entry("genre", List.of("Hành động", "Viễn tưởng", "Phiêu lưu")), Map.entry("releaseDate", ts("2025-03-01T00:00:00Z")), Map.entry("director", "Denis Villeneuve"), Map.entry("cast", List.of("Timothée Chalamet", "Zendaya"))
                ),
                Map.ofEntries(
                        Map.entry("movieId", "m_003"), Map.entry("title", "Mai"), Map.entry("description", "Câu chuyện tình yêu của Mai, một nhân viên massage, và Dương, một nhạc công."), Map.entry("posterUrl", "https://picsum.photos/seed/mai/200/300"), Map.entry("backdropUrl", "https://picsum.photos/seed/mai_bg/400/200"), Map.entry("trailerUrl", "https://www.youtube.com/watch?v=..."), Map.entry("durationMinutes", 131), Map.entry("genre", List.of("Tình cảm", "Chính kịch")), Map.entry("releaseDate", ts("2025-02-10T00:00:00Z")), Map.entry("director", "Trấn Thành"), Map.entry("cast", List.of("Phương Anh Đào", "Tuấn Trần"))
                )
        );
        System.out.println("... Seeding movies ...");
        for (Map<String, Object> movie : movies) {
            db.collection("movies").document((String) movie.get("movieId")).set(movie).get();
        }
    }

    // 3. Thêm Rạp (theaters) và Phòng (rooms)
    private void seedTheatersAndRooms() throws Exception {
        List<Map<String, Object>> theaters = List.of(
                Map.of("theaterId", "beta_001", "name", "Beta Cinemas Thanh Xuân", "address", "Tầng hầm B1, Vincom Royal City, Hà Nội", "city", "Hà Nội", "location", new GeoPoint(21.005, 105.816)),
                Map.of("theaterId", "beta_002", "name", "Beta Cinemas Mỹ Đình", "address", "Tầng hầm B1, The Garden, Nam Từ Liêm, Hà Nội", "city", "Hà Nội", "location", new GeoPoint(21.028, 105.776)),
                Map.of("theaterId", "beta_003", "name", "Beta Cinemas Quang Trung", "address", "645 Quang Trung, Gò Vấp, TP. Hồ Chí Minh", "city", "TP. Hồ Chí Minh", "location", new GeoPoint(10.835, 106.666)),
                Map.of("theaterId", "beta_004", "name", "Beta Cinemas Empire", "address", "Tầng 3, TTTM Empire, Quận 1, TP. Hồ Chí Minh", "city", "TP. Hồ Chí Minh", "location", new GeoPoint(10.772, 106.704)),
                Map.of("theaterId", "beta_005", "name", "Beta Cinemas Vĩnh Trung", "address", "255 Hùng Vương, Vĩnh Trung, Đà Nẵng", "city", "Đà Nẵng", "location", new GeoPoint(16.068, 108.217))
        );
        System.out.println("... Seeding theaters ...");
        for (Map<String, Object> theater : theaters) {
            db.collection("theaters").document((String) theater.get("theaterId")).set(theater).get();
        }

        List<Map<String, Object>> rooms = List.of(
                Map.of("roomId", "r_001", "theaterId", "beta_001", "name", "Phòng 1", "capacity", 150),
                Map.of("roomId", "r_002", "theaterId", "beta_001", "name", "Phòng 2", "capacity", 120),
                Map.of("roomId", "r_003", "theaterId", "beta_002", "name", "Phòng 1", "capacity", 200),
                Map.of("roomId", "r_004", "theaterId", "beta_002", "name", "Phòng 2 (VIP)", "capacity", 80),
                Map.of("roomId", "r_005", "theaterId", "beta_003", "name", "Phòng 1", "capacity", 180),
                Map.of("roomId", "r_006", "theaterId", "beta_003", "name", "Phòng 2", "capacity", 180),
                Map.of("roomId", "r_007", "theaterId", "beta_004", "name", "Phòng Gold Class", "capacity", 50),
                Map.of("roomId", "r_008", "theaterId", "beta_005", "name", "Phòng 1", "capacity", 160),
                Map.of("roomId", "r_009", "theaterId", "beta_005", "name", "Phòng 2", "capacity", 160)
        );
        System.out.println("... Seeding rooms (sub-collection) ...");
        for (Map<String, Object> room : rooms) {
            // String theaterId = (String) room.get("theaterId"); // <<< DÒNG CŨ
            String roomId = (String) room.get("roomId");

            // Sửa: Lưu vào collection "rooms" ở gốc
            // db.collection("theaters").document(theaterId).collection("rooms").document(roomId).set(room).get(); // <<< DÒNG CŨ
            db.collection("rooms").document(roomId).set(room).get(); // <<< DÒNG MỚI
        }
    }

    // 4. Thêm Suất chiếu (showtimes)
    private void seedShowtimes() throws Exception {
        // Lấy một ngày trong tương lai gần để đảm bảo vé là "VALID"
        String futureTime1 = "2025-11-20T19:00:00Z";
        String futureTime2 = "2025-11-20T21:30:00Z";

        List<Map<String, Object>> showtimes = List.of(
                // === DỮ LIỆU LIÊN KẾT CHO PHIM "LẬT MẶT 7" (m_001) TẠI 5 RẠP ===
                // Rạp 1: beta_001 (Thanh Xuân)
                Map.of("showtimeId", "st_001", "movieId", "m_001", "theaterId", "beta_001", "roomId", "r_001", "startTime", ts(futureTime1), "ticketPrice", 90000.0, "totalTickets", 150, "availableTickets", 150),
                Map.of("showtimeId", "st_002", "movieId", "m_001", "theaterId", "beta_001", "roomId", "r_002", "startTime", ts(futureTime2), "ticketPrice", 90000.0, "totalTickets", 120, "availableTickets", 120),
                // Rạp 2: beta_002 (Mỹ Đình)
                Map.of("showtimeId", "st_003", "movieId", "m_001", "theaterId", "beta_002", "roomId", "r_003", "startTime", ts(futureTime1), "ticketPrice", 95000.0, "totalTickets", 200, "availableTickets", 200),
                Map.of("showtimeId", "st_004", "movieId", "m_001", "theaterId", "beta_002", "roomId", "r_004", "startTime", ts(futureTime2), "ticketPrice", 150000.0, "totalTickets", 80, "availableTickets", 80),
                // Rạp 3: beta_003 (Quang Trung)
                Map.of("showtimeId", "st_005", "movieId", "m_001", "theaterId", "beta_003", "roomId", "r_005", "startTime", ts(futureTime1), "ticketPrice", 100000.0, "totalTickets", 180, "availableTickets", 180),
                Map.of("showtimeId", "st_006", "movieId", "m_001", "theaterId", "beta_003", "roomId", "r_006", "startTime", ts(futureTime2), "ticketPrice", 100000.0, "totalTickets", 180, "availableTickets", 180),
                // Rạp 4: beta_004 (Empire)
                Map.of("showtimeId", "st_007", "movieId", "m_001", "theaterId", "beta_004", "roomId", "r_007", "startTime", ts(futureTime1), "ticketPrice", 250000.0, "totalTickets", 50, "availableTickets", 50),
                Map.of("showtimeId", "st_008", "movieId", "m_001", "theaterId", "beta_004", "roomId", "r_007", "startTime", ts(futureTime2), "ticketPrice", 250000.0, "totalTickets", 50, "availableTickets", 50),
                // Rạp 5: beta_005 (Vĩnh Trung)
                Map.of("showtimeId", "st_009", "movieId", "m_001", "theaterId", "beta_005", "roomId", "r_008", "startTime", ts(futureTime1), "ticketPrice", 85000.0, "totalTickets", 160, "availableTickets", 160),
                Map.of("showtimeId", "st_010", "movieId", "m_001", "theaterId", "beta_005", "roomId", "r_009", "startTime", ts(futureTime2), "ticketPrice", 85000.0, "totalTickets", 160, "availableTickets", 160),

                // === DỮ LIỆU CHO CÁC PHIM KHÁC (để test) ===
                Map.of("showtimeId", "st_011", "movieId", "m_002", "theaterId", "beta_001", "roomId", "r_001", "startTime", ts(futureTime2), "ticketPrice", 120000.0, "totalTickets", 150, "availableTickets", 140),
                Map.of("showtimeId", "st_012", "movieId", "m_003", "theaterId", "beta_003", "roomId", "r_005", "startTime", ts(futureTime1), "ticketPrice", 100000.0, "totalTickets", 180, "availableTickets", 180)
        );
        System.out.println("... Seeding showtimes (đã liên kết) ...");
        for (Map<String, Object> showtime : showtimes) {
            db.collection("showtimes").document((String) showtime.get("showtimeId")).set(showtime).get();
        }
    }

    // 5. Thêm Đơn đặt vé (bookings)
    private void seedBookings() throws Exception {
        List<Map<String, Object>> bookings = List.of(
                // Đơn hàng thành công cho user_001, đặt suất st_001 (Lật Mặt 7 @ Thanh Xuân)
                Map.ofEntries(Map.entry("bookingId", "b_001"), Map.entry("userId", "user_001"), Map.entry("showtimeId", "st_001"), Map.entry("quantity", 2), Map.entry("totalAmount", 180000.0), Map.entry("status", "SUCCESSFUL"), Map.entry("createdAt", ts("2025-11-05T10:00:00Z"))),
                // Đơn hàng PENDING cho user_002
                Map.ofEntries(Map.entry("bookingId", "b_002"), Map.entry("userId", "user_002"), Map.entry("showtimeId", "st_011"), Map.entry("quantity", 1), Map.entry("totalAmount", 120000.0), Map.entry("status", "PENDING"), Map.entry("createdAt", ts("2025-11-05T10:10:00Z")))
        );
        System.out.println("... Seeding bookings ...");
        for (Map<String, Object> booking : bookings) {
            db.collection("bookings").document((String) booking.get("bookingId")).set(booking).get();
        }
    }

    // 6. Thêm Vé (tickets) - Liên kết từ booking b_001
    private void seedTickets() throws Exception {
        List<Map<String, Object>> tickets = List.of(
                // 2 vé (VALID) cho booking b_001
                Map.ofEntries(Map.entry("ticketId", "t_001"), Map.entry("bookingId", "b_001"), Map.entry("userId", "user_001"), Map.entry("qrCodeData", "t_001"), Map.entry("status", "VALID"), Map.entry("movieTitle", "Lật Mặt 7: Một Điều Ước"), Map.entry("theaterName", "Beta Cinemas Thanh Xuân"), Map.entry("roomName", "Phòng 1"), Map.entry("showtimeStartTime", ts("2025-11-20T19:00:00Z"))),
                Map.ofEntries(Map.entry("ticketId", "t_002"), Map.entry("bookingId", "b_001"), Map.entry("userId", "user_001"), Map.entry("qrCodeData", "t_002"), Map.entry("status", "VALID"), Map.entry("movieTitle", "Lật Mặt 7: Một Điều Ước"), Map.entry("theaterName", "Beta Cinemas Thanh Xuân"), Map.entry("roomName", "Phòng 1"), Map.entry("showtimeStartTime", ts("2025-11-20T19:00:00Z")))
        );
        System.out.println("... Seeding tickets (đã liên kết) ...");
        for (Map<String, Object> ticket : tickets) {
            db.collection("tickets").document((String) ticket.get("ticketId")).set(ticket).get();
        }
    }

    // 7. Thêm Đánh giá (reviews) - (Đã sửa rating thành double, createdAt thành Timestamp)
    private void seedReviews() throws Exception {
        List<Map<String, Object>> reviews = List.of(
                Map.of("reviewId", "rv_001", "movieId", "m_001", "userId", "user_001", "rating", 5.0, "comment", "Phim hay và cảm động, nên xem!", "createdAt", ts("2025-05-01T14:00:00Z"), "userFullName", "Nguyễn Văn A"),
                Map.of("reviewId", "rv_002", "movieId", "m_002", "userId", "user_002", "rating", 4.5, "comment", "Kỹ xảo mãn nhãn, nội dung hoành tráng.", "createdAt", ts("2025-03-10T20:00:00Z"), "userFullName", "Trần Văn B")
        );
        System.out.println("... Seeding reviews ...");
        for (Map<String, Object> review : reviews) {
            db.collection("reviews").document((String) review.get("reviewId")).set(review).get();
        }
    }

    // 8. Thêm Token thiết bị (device_tokens)
    private void seedDeviceTokens() throws Exception {
        List<Map<String, Object>> tokens = List.of(
                Map.of("userId", "user_001", "tokens", List.of("fcm_token_user_1_device_1...")),
                Map.of("userId", "user_002", "tokens", List.of("fcm_token_user_2_device_1..."))
        );
        System.out.println("... Seeding device_tokens ...");
        for (Map<String, Object> token : tokens) {
            db.collection("device_tokens").document((String) token.get("userId")).set(token).get();
        }
    }
}