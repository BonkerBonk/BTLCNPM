package com.btlcnpm.SearchService.service; // <-- (QUAN TRỌNG) Đảm bảo tên package này đúng

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.GeoPoint;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component // Báo cho Spring Boot biết để quản lý file này
public class DataSeeder {

    private final Firestore db;

    // Tiêm (inject) Firestore vào
    public DataSeeder(Firestore firestore) {
        this.db = firestore;
    }

    // @PostConstruct sẽ khiến hàm này tự động chạy MỘT LẦN khi service khởi động
    //@PostConstruct
    public void seedDatabase() {
        System.out.println("--- BẮT ĐẦU THÊM DỮ LIỆU MẪU (SEEDING) ---");
        try {
            // Thêm dữ liệu theo thứ tự (để đảm bảo có ID cho các bước sau)
            seedUsers();
            seedMovies(); // ĐÃ SỬA
            seedTheatersAndRooms();
            seedShowtimes();
            seedBookings();
            seedTickets(); // ĐÃ SỬA
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

    // 1. Thêm 10 Người dùng (users)
    private void seedUsers() throws Exception {
        List<Map<String, Object>> users = List.of(
                Map.of("userId", "user_001", "email", "nguyenvana@gmail.com", "fullName", "Nguyễn Văn A", "phoneNumber", "0901234567", "profileImageUrl", "https://picsum.photos/seed/user_001/200", "dateOfBirth", ts("1995-10-20T00:00:00Z"), "createdAt", ts("2025-01-15T09:30:00Z")),
                Map.of("userId", "user_002", "email", "tranvanb@gmail.com", "fullName", "Trần Văn B", "phoneNumber", "0902345678", "profileImageUrl", "https://picsum.photos/seed/user_002/200", "dateOfBirth", ts("1998-05-12T00:00:00Z"), "createdAt", ts("2025-01-16T10:00:00Z")),
                Map.of("userId", "user_003", "email", "lethic@gmail.com", "fullName", "Lê Thị C", "phoneNumber", "0903456789", "profileImageUrl", "https://picsum.photos/seed/user_003/200", "dateOfBirth", ts("2000-02-25T00:00:00Z"), "createdAt", ts("2025-01-17T11:45:00Z")),
                Map.of("userId", "user_004", "email", "phamvand@gmail.com", "fullName", "Phạm Văn D", "phoneNumber", "0904567890", "profileImageUrl", "https://picsum.photos/seed/user_004/200", "dateOfBirth", ts("1992-11-30T00:00:00Z"), "createdAt", ts("2025-01-18T14:20:00Z")),
                Map.of("userId", "user_005", "email", "hoangthie@gmail.com", "fullName", "Hoàng Thị E", "phoneNumber", "0905678901", "profileImageUrl", "https://picsum.photos/seed/user_005/200", "dateOfBirth", ts("2001-07-19T00:00:00Z"), "createdAt", ts("2025-01-19T16:05:00Z")),
                Map.of("userId", "user_006", "email", "vovang@gmail.com", "fullName", "Võ Văn G", "phoneNumber", "0906789012", "profileImageUrl", "https://picsum.photos/seed/user_006/200", "dateOfBirth", ts("1999-03-14T00:00:00Z"), "createdAt", ts("2025-01-20T08:15:00Z")),
                Map.of("userId", "user_007", "email", "dangthih@gmail.com", "fullName", "Đặng Thị H", "phoneNumber", "0907890123", "profileImageUrl", "https://picsum.photos/seed/user_007/200", "dateOfBirth", ts("1997-09-05T00:00:00Z"), "createdAt", ts("2025-01-21T13:30:00Z")),
                Map.of("userId", "user_008", "email", "buihienk@gmail.com", "fullName", "Bùi Hiển K", "phoneNumber", "0908901234", "profileImageUrl", "https://picsum.photos/seed/user_008/200", "dateOfBirth", ts("1996-12-22T00:00:00Z"), "createdAt", ts("2025-01-22T15:00:00Z")),
                Map.of("userId", "user_009", "email", "dothil@gmail.com", "fullName", "Đỗ Thị L", "phoneNumber", "0909012345", "profileImageUrl", "https://picsum.photos/seed/user_009/200", "dateOfBirth", ts("2002-04-10T00:00:00Z"), "createdAt", ts("2025-01-23T17:55:00Z")),
                Map.of("userId", "user_010", "email", "ngominhn@gmail.com", "fullName", "Ngô Minh N", "phoneNumber", "0911234567", "profileImageUrl", "https://picsum.photos/seed/user_010/200", "dateOfBirth", ts("1994-08-01T00:00:00Z"), "createdAt", ts("2025-01-24T18:30:00Z"))
        );
        System.out.println("... Seeding users ...");
        for (Map<String, Object> user : users) {
            db.collection("users").document((String) user.get("userId")).set(user).get();
        }
    }

    // 2. Thêm 10 Phim (movies) - ĐÃ SỬA LỖI
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
                ),
                Map.ofEntries(
                        Map.entry("movieId", "m_004"), Map.entry("title", "Inside Out 2"), Map.entry("description", "Riley bước vào tuổi dậy thì với những cảm xúc mới: Lo Âu, Ganh Tị, Xấu Hổ."), Map.entry("posterUrl", "https://picsum.photos/seed/insideout2/200/300"), Map.entry("backdropUrl", "https://picsum.photos/seed/insideout2_bg/400/200"), Map.entry("trailerUrl", "https://www.youtube.com/watch?v=..."), Map.entry("durationMinutes", 96), Map.entry("genre", List.of("Hoạt hình", "Gia đình", "Hài")), Map.entry("releaseDate", ts("2025-06-14T00:00:00Z")), Map.entry("director", "Kelsey Mann"), Map.entry("cast", List.of("Amy Poehler", "Maya Hawke"))
                ),
                Map.ofEntries(
                        Map.entry("movieId", "m_005"), Map.entry("title", "Oppenheimer"), Map.entry("description", "Câu chuyện về J. Robert Oppenheimer, cha đẻ của bom nguyên tử."), Map.entry("posterUrl", "https://picsum.photos/seed/oppenheimer/200/300"), Map.entry("backdropUrl", "https://picsum.photos/seed/oppenheimer_bg/400/200"), Map.entry("trailerUrl", "https://www.youtube.com/watch?v=..."), Map.entry("durationMinutes", 180), Map.entry("genre", List.of("Tiểu sử", "Chính kịch", "Lịch sử")), Map.entry("releaseDate", ts("2024-07-21T00:00:00Z")), Map.entry("director", "Christopher Nolan"), Map.entry("cast", List.of("Cillian Murphy", "Emily Blunt"))
                ),
                Map.ofEntries(
                        Map.entry("movieId", "m_006"), Map.entry("title", "Kẻ Trộm Mặt Trăng 4"), Map.entry("description", "Gru và gia đình chào đón thành viên mới, Gru Jr., và đối mặt với kẻ thù mới."), Map.entry("posterUrl", "https://picsum.photos/seed/dm4/200/300"), Map.entry("backdropUrl", "https://picsum.photos/seed/dm4_bg/400/200"), Map.entry("trailerUrl", "https://www.youtube.com/watch?v=..."), Map.entry("durationMinutes", 95), Map.entry("genre", List.of("Hoạt hình", "Hài", "Phiêu lưu")), Map.entry("releaseDate", ts("2025-07-03T00:00:00Z")), Map.entry("director", "Chris Renaud"), Map.entry("cast", List.of("Steve Carell", "Kristen Wiig"))
                ),
                Map.ofEntries(
                        Map.entry("movieId", "m_007"), Map.entry("title", "Gia Tài Của Ngoại"), Map.entry("description", "Chàng trai M về chăm sóc bà ngoại đang bị ung thư với hy vọng được thừa kế."), Map.entry("posterUrl", "https://picsum.photos/seed/giataicuaNgoai/200/300"), Map.entry("backdropUrl", "https://picsum.photos/seed/giataicuaNgoai_bg/400/200"), Map.entry("trailerUrl", "https://www.youtube.com/watch?v=..."), Map.entry("durationMinutes", 127), Map.entry("genre", List.of("Gia đình", "Chính kịch")), Map.entry("releaseDate", ts("2025-06-07T00:00:00Z")), Map.entry("director", "Pat Boonnitipat"), Map.entry("cast", List.of("Putthipong Assaratanakul", "Usha Seamkhum"))
                ),
                Map.ofEntries(
                        Map.entry("movieId", "m_008"), Map.entry("title", "Godzilla x Kong: Đế Chế Mới"), Map.entry("description", "Kong và Godzilla hợp lực chống lại một mối đe dọa khổng lồ mới từ Trái Đất Rỗng."), Map.entry("posterUrl", "https://picsum.photos/seed/gvk2/200/300"), Map.entry("backdropUrl", "https://picsum.photos/seed/gvk2_bg/400/200"), Map.entry("trailerUrl", "https://www.youtube.com/watch?v=..."), Map.entry("durationMinutes", 115), Map.entry("genre", List.of("Hành động", "Viễn tưởng", "Quái vật")), Map.entry("releaseDate", ts("2025-03-29T00:00:00Z")), Map.entry("director", "Adam Wingard"), Map.entry("cast", List.of("Rebecca Hall", "Dan Stevens"))
                ),
                Map.ofEntries(
                        Map.entry("movieId", "m_009"), Map.entry("title", "Vùng Đất Câm Lặng: Ngày Một"), Map.entry("description", "Ngày đầu tiên thế giới bị xâm lược bởi những sinh vật săn mồi bằng âm thanh."), Map.entry("posterUrl", "https://picsum.photos/seed/quietplace1/200/300"), Map.entry("backdropUrl", "https://picsum.photos/seed/quietplace1_bg/400/200"), Map.entry("trailerUrl", "https://www.youtube.com/watch?v=..."), Map.entry("durationMinutes", 100), Map.entry("genre", List.of("Kinh dị", "Giật gân", "Viễn tưởng")), Map.entry("releaseDate", ts("2025-06-28T00:00:00Z")), Map.entry("director", "Michael Sarnoski"), Map.entry("cast", List.of("Lupita Nyong'o", "Joseph Quinn"))
                ),
                Map.ofEntries(
                        Map.entry("movieId", "m_010"), Map.entry("title", "Kung Fu Panda 4"), Map.entry("description", "Po phải tìm người kế vị Thần Long Đại Hiệp và chiến đấu với Tắc Kè Bông."), Map.entry("posterUrl", "https://picsum.photos/seed/kfp4/200/300"), Map.entry("backdropUrl", "https://picsum.photos/seed/kfp4_bg/400/200"), Map.entry("trailerUrl", "https://www.youtube.com/watch?v=..."), Map.entry("durationMinutes", 94), Map.entry("genre", List.of("Hoạt hình", "Hài", "Võ thuật")), Map.entry("releaseDate", ts("2025-03-08T00:00:00Z")), Map.entry("director", "Mike Mitchell"), Map.entry("cast", List.of("Jack Black", "Awkwafina"))
                )
        );
        System.out.println("... Seeding movies ...");
        for (Map<String, Object> movie : movies) {
            db.collection("movies").document((String) movie.get("movieId")).set(movie).get();
        }
    }

    // 3. Thêm 10 Rạp (theaters) và 10 Phòng (rooms)
    private void seedTheatersAndRooms() throws Exception {
        List<Map<String, Object>> theaters = List.of(
                Map.of("theaterId", "beta_001", "name", "Beta Cinemas Thanh Xuân", "address", "Tầng hầm B1, Vincom Royal City, Hà Nội", "city", "Hà Nội", "location", new GeoPoint(21.005, 105.816)),
                Map.of("theaterId", "beta_002", "name", "Beta Cinemas Mỹ Đình", "address", "Tầng hầm B1, The Garden, Nam Từ Liêm, Hà Nội", "city", "Hà Nội", "location", new GeoPoint(21.028, 105.776)),
                Map.of("theaterId", "beta_003", "name", "Beta Cinemas Quang Trung", "address", "645 Quang Trung, Gò Vấp, TP. Hồ Chí Minh", "city", "TP. Hồ Chí Minh", "location", new GeoPoint(10.835, 106.666)),
                Map.of("theaterId", "beta_004", "name", "Beta Cinemas Empire", "address", "Tầng 3, TTTM Empire, Quận 1, TP. Hồ Chí Minh", "city", "TP. Hồ Chí Minh", "location", new GeoPoint(10.772, 106.704)),
                Map.of("theaterId", "beta_005", "name", "Beta Cinemas Vĩnh Trung", "address", "255 Hùng Vương, Vĩnh Trung, Đà Nẵng", "city", "Đà Nẵng", "location", new GeoPoint(16.068, 108.217)),
                Map.of("theaterId", "beta_006", "name", "Beta Cinemas Long Xuyên", "address", "TTTM Vincom Plaza, Trần Hưng Đạo, An Giang", "city", "An Giang", "location", new GeoPoint(10.383, 105.426)),
                Map.of("theaterId", "beta_007", "name", "Beta Cinemas Biên Hòa", "address", "123 Võ Thị Sáu, Thống Nhất, Biên Hòa, Đồng Nai", "city", "Đồng Nai", "location", new GeoPoint(10.949, 106.824)),
                Map.of("theaterId", "beta_008", "name", "Beta Cinemas Cần Thơ", "address", "Vincom Xuân Khánh, Ninh Kiều, Cần Thơ", "city", "Cần Thơ", "location", new GeoPoint(10.035, 105.772)),
                Map.of("theaterId", "beta_009", "name", "Beta Cinemas Hải Phòng", "address", "Vincom Imperia, Thượng Lý, Hồng Bàng, Hải Phòng", "city", "Hải Phòng", "location", new GeoPoint(20.871, 106.671)),
                Map.of("theaterId", "beta_010", "name", "Beta Cinemas Nha Trang", "address", "78-80 Trần Phú, Lộc Thọ, Nha Trang", "city", "Khánh Hòa", "location", new GeoPoint(12.238, 109.197))
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
                Map.of("roomId", "r_009", "theaterId", "beta_005", "name", "Phòng 2", "capacity", 160),
                Map.of("roomId", "r_010", "theaterId", "beta_006", "name", "Phòng 1", "capacity", 130)
        );
        System.out.println("... Seeding rooms (sub-collection) ...");
        for (Map<String, Object> room : rooms) {
            String theaterId = (String) room.get("theaterId");
            String roomId = (String) room.get("roomId");
            db.collection("theaters").document(theaterId).collection("rooms").document(roomId).set(room).get();
        }
    }

    // 4. Thêm 10 Suất chiếu (showtimes)
    private void seedShowtimes() throws Exception {
        List<Map<String, Object>> showtimes = List.of(
                Map.of("showtimeId", "st_001", "movieId", "m_001", "theaterId", "beta_001", "roomId", "r_001", "startTime", ts("2025-10-26T19:00:00Z"), "ticketPrice", 90000, "totalTickets", 150, "availableTickets", 150),
                Map.of("showtimeId", "st_002", "movieId", "m_001", "theaterId", "beta_001", "roomId", "r_002", "startTime", ts("2025-10-26T21:00:00Z"), "ticketPrice", 90000, "totalTickets", 120, "availableTickets", 120),
                Map.of("showtimeId", "st_003", "movieId", "m_002", "theaterId", "beta_001", "roomId", "r_001", "startTime", ts("2025-10-26T20:00:00Z"), "ticketPrice", 120000, "totalTickets", 150, "availableTickets", 140),
                Map.of("showtimeId", "st_004", "movieId", "m_003", "theaterId", "beta_003", "roomId", "r_005", "startTime", ts("2025-10-27T18:30:00Z"), "ticketPrice", 100000, "totalTickets", 180, "availableTickets", 180),
                Map.of("showtimeId", "st_005", "movieId", "m_004", "theaterId", "beta_003", "roomId", "r_006", "startTime", ts("2025-10-27T19:15:00Z"), "ticketPrice", 95000, "totalTickets", 180, "availableTickets", 170),
                Map.of("showtimeId", "st_006", "movieId", "m_002", "theaterId", "beta_005", "roomId", "r_008", "startTime", ts("2025-10-28T19:00:00Z"), "ticketPrice", 110000, "totalTickets", 160, "availableTickets", 160),
                Map.of("showtimeId", "st_007", "movieId", "m_007", "theaterId", "beta_002", "roomId", "r_003", "startTime", ts("2025-10-28T20:30:00Z"), "ticketPrice", 85000, "totalTickets", 200, "availableTickets", 200),
                Map.of("showtimeId", "st_008", "movieId", "m_008", "theaterId", "beta_004", "roomId", "r_007", "startTime", ts("2025-10-28T21:00:00Z"), "ticketPrice", 250000, "totalTickets", 50, "availableTickets", 50),
                Map.of("showtimeId", "st_009", "movieId", "m_009", "theaterId", "beta_001", "roomId", "r_002", "startTime", ts("2025-10-27T22:00:00Z"), "ticketPrice", 100000, "totalTickets", 120, "availableTickets", 110),
                Map.of("showtimeId", "st_010", "movieId", "m_010", "theaterId", "beta_003", "roomId", "r_005", "startTime", ts("2025-10-27T17:00:00Z"), "ticketPrice", 80000, "totalTickets", 180, "availableTickets", 180)
        );
        System.out.println("... Seeding showtimes ...");
        for (Map<String, Object> showtime : showtimes) {
            db.collection("showtimes").document((String) showtime.get("showtimeId")).set(showtime).get();
        }
    }

    // 5. Thêm 10 Đơn đặt vé (bookings)
    private void seedBookings() throws Exception {
        // Cần dùng Map.ofEntries vì có thể có trường 'expiresAt' (null)
        // Map.of() không cho phép giá trị null, nhưng Map.ofEntries thì có thể
        List<Map<String, Object>> bookings = List.of(
                Map.ofEntries(Map.entry("bookingId", "b_001"), Map.entry("userId", "user_001"), Map.entry("showtimeId", "st_001"), Map.entry("quantity", 2), Map.entry("totalAmount", 180000), Map.entry("status", "SUCCESSFUL"), Map.entry("createdAt", ts("2025-10-25T10:00:00Z"))),
                Map.ofEntries(Map.entry("bookingId", "b_002"), Map.entry("userId", "user_002"), Map.entry("showtimeId", "st_003"), Map.entry("quantity", 3), Map.entry("totalAmount", 360000), Map.entry("status", "SUCCESSFUL"), Map.entry("createdAt", ts("2025-10-25T10:05:00Z"))),
                Map.ofEntries(Map.entry("bookingId", "b_003"), Map.entry("userId", "user_003"), Map.entry("showtimeId", "st_005"), Map.entry("quantity", 2), Map.entry("totalAmount", 190000), Map.entry("status", "PENDING"), Map.entry("createdAt", ts("2025-10-25T10:10:00Z")), Map.entry("expiresAt", ts("2025-10-25T10:20:00Z"))),
                Map.ofEntries(Map.entry("bookingId", "b_004"), Map.entry("userId", "user_004"), Map.entry("showtimeId", "st_008"), Map.entry("quantity", 2), Map.entry("totalAmount", 500000), Map.entry("status", "SUCCESSFUL"), Map.entry("createdAt", ts("2025-10-25T11:00:00Z"))),
                Map.ofEntries(Map.entry("bookingId", "b_005"), Map.entry("userId", "user_001"), Map.entry("showtimeId", "st_004"), Map.entry("quantity", 1), Map.entry("totalAmount", 100000), Map.entry("status", "FAILED"), Map.entry("createdAt", ts("2025-10-25T11:30:00Z"))),
                Map.ofEntries(Map.entry("bookingId", "b_006"), Map.entry("userId", "user_005"), Map.entry("showtimeId", "st_007"), Map.entry("quantity", 4), Map.entry("totalAmount", 340000), Map.entry("status", "SUCCESSFUL"), Map.entry("createdAt", ts("2025-10-25T12:00:00Z"))),
                Map.ofEntries(Map.entry("bookingId", "b_007"), Map.entry("userId", "user_006"), Map.entry("showtimeId", "st_009"), Map.entry("quantity", 2), Map.entry("totalAmount", 200000), Map.entry("status", "PENDING"), Map.entry("createdAt", ts("2025-10-25T12:05:00Z")), Map.entry("expiresAt", ts("2025-10-25T12:15:00Z"))),
                Map.ofEntries(Map.entry("bookingId", "b_008"), Map.entry("userId", "user_007"), Map.entry("showtimeId", "st_010"), Map.entry("quantity", 5), Map.entry("totalAmount", 400000), Map.entry("status", "SUCCESSFUL"), Map.entry("createdAt", ts("2025-10-25T13:00:00Z"))),
                Map.ofEntries(Map.entry("bookingId", "b_009"), Map.entry("userId", "user_008"), Map.entry("showtimeId", "st_002"), Map.entry("quantity", 2), Map.entry("totalAmount", 180000), Map.entry("status", "SUCCESSFUL"), Map.entry("createdAt", ts("2025-10-25T13:10:00Z"))),
                Map.ofEntries(Map.entry("bookingId", "b_010"), Map.entry("userId", "user_009"), Map.entry("showtimeId", "st_006"), Map.entry("quantity", 2), Map.entry("totalAmount", 220000), Map.entry("status", "CANCELLED"), Map.entry("createdAt", ts("2025-10-25T13:15:00Z")))
        );
        System.out.println("... Seeding bookings ...");
        for (Map<String, Object> booking : bookings) {
            db.collection("bookings").document((String) booking.get("bookingId")).set(booking).get();
        }
    }

    // 6. Thêm 10 Vé (tickets) - ĐÃ SỬA LỖI
    private void seedTickets() throws Exception {
        // Dùng Map.ofEntries vì có 11 trường
        List<Map<String, Object>> tickets = List.of(
                Map.ofEntries(Map.entry("ticketId", "t_001"), Map.entry("bookingId", "b_001"), Map.entry("userId", "user_001"), Map.entry("qrCodeData", "t_001_b_001_u_001"), Map.entry("status", "VALID"), Map.entry("movieTitle", "Lật Mặt 7: Một Điều Ước"), Map.entry("theaterName", "Beta Cinemas Thanh Xuân"), Map.entry("roomName", "Phòng 1"), Map.entry("showtimeStartTime", ts("2025-10-26T19:00:00Z"))),
                Map.ofEntries(Map.entry("ticketId", "t_002"), Map.entry("bookingId", "b_002"), Map.entry("userId", "user_002"), Map.entry("qrCodeData", "t_002_b_002_u_002"), Map.entry("status", "VALID"), Map.entry("movieTitle", "Dune: Hành Tinh Cát 2"), Map.entry("theaterName", "Beta Cinemas Thanh Xuân"), Map.entry("roomName", "Phòng 1"), Map.entry("showtimeStartTime", ts("2025-10-26T20:00:00Z"))),
                Map.ofEntries(Map.entry("ticketId", "t_003"), Map.entry("bookingId", "b_004"), Map.entry("userId", "user_004"), Map.entry("qrCodeData", "t_003_b_004_u_004"), Map.entry("status", "USED"), Map.entry("movieTitle", "Godzilla x Kong: Đế Chế Mới"), Map.entry("theaterName", "Beta Cinemas Empire"), Map.entry("roomName", "Phòng Gold Class"), Map.entry("showtimeStartTime", ts("2025-10-28T21:00:00Z"))),
                Map.ofEntries(Map.entry("ticketId", "t_004"), Map.entry("bookingId", "b_006"), Map.entry("userId", "user_005"), Map.entry("qrCodeData", "t_004_b_006_u_005"), Map.entry("status", "VALID"), Map.entry("movieTitle", "Gia Tài Của Ngoại"), Map.entry("theaterName", "Beta Cinemas Mỹ Đình"), Map.entry("roomName", "Phòng 1"), Map.entry("showtimeStartTime", ts("2025-10-28T20:30:00Z"))),
                Map.ofEntries(Map.entry("ticketId", "t_005"), Map.entry("bookingId", "b_008"), Map.entry("userId", "user_007"), Map.entry("qrCodeData", "t_005_b_008_u_007"), Map.entry("status", "EXPIRED"), Map.entry("movieTitle", "Kung Fu Panda 4"), Map.entry("theaterName", "Beta Cinemas Quang Trung"), Map.entry("roomName", "Phòng 1"), Map.entry("showtimeStartTime", ts("2025-10-27T17:00:00Z"))),
                Map.ofEntries(Map.entry("ticketId", "t_006"), Map.entry("bookingId", "b_009"), Map.entry("userId", "user_008"), Map.entry("qrCodeData", "t_006_b_009_u_008"), Map.entry("status", "VALID"), Map.entry("movieTitle", "Lật Mặt 7: Một Điều Ước"), Map.entry("theaterName", "Beta Cinemas Thanh Xuân"), Map.entry("roomName", "Phòng 2"), Map.entry("showtimeStartTime", ts("2025-10-26T21:00:00Z"))),
                Map.ofEntries(Map.entry("ticketId", "t_007"), Map.entry("bookingId", "b_001"), Map.entry("userId", "user_001"), Map.entry("qrCodeData", "t_007_b_001_u_001_2"), Map.entry("status", "VALID"), Map.entry("movieTitle", "Lật Mặt 7: Một Điều Ước"), Map.entry("theaterName", "Beta Cinemas Thanh Xuân"), Map.entry("roomName", "Phòng 1"), Map.entry("showtimeStartTime", ts("2025-10-26T19:00:00Z"))),
                Map.ofEntries(Map.entry("ticketId", "t_008"), Map.entry("bookingId", "b_002"), Map.entry("userId", "user_002"), Map.entry("qrCodeData", "t_008_b_002_u_002_2"), Map.entry("status", "VALID"), Map.entry("movieTitle", "Dune: Hành Tinh Cát 2"), Map.entry("theaterName", "Beta Cinemas Thanh Xuân"), Map.entry("roomName", "Phòng 1"), Map.entry("showtimeStartTime", ts("2025-10-26T20:00:00Z"))),
                Map.ofEntries(Map.entry("ticketId", "t_009"), Map.entry("bookingId", "b_002"), Map.entry("userId", "user_002"), Map.entry("qrCodeData", "t_009_b_002_u_002_3"), Map.entry("status", "VALID"), Map.entry("movieTitle", "Dune: Hành Tinh Cát 2"), Map.entry("theaterName", "Beta Cinemas Thanh Xuân"), Map.entry("roomName", "Phòng 1"), Map.entry("showtimeStartTime", ts("2025-10-26T20:00:00Z"))),
                Map.ofEntries(Map.entry("ticketId", "t_010"), Map.entry("bookingId", "b_004"), Map.entry("userId", "user_004"), Map.entry("qrCodeData", "t_010_b_004_u_004_2"), Map.entry("status", "USED"), Map.entry("movieTitle", "Godzilla x Kong: Đế Chế Mới"), Map.entry("theaterName", "Beta Cinemas Empire"), Map.entry("roomName", "Phòng Gold Class"), Map.entry("showtimeStartTime", ts("2025-10-28T21:00:00Z")))
        );
        System.out.println("... Seeding tickets ...");
        for (Map<String, Object> ticket : tickets) {
            db.collection("tickets").document((String) ticket.get("ticketId")).set(ticket).get();
        }
    }

    // 7. Thêm 10 Đánh giá (reviews)
    private void seedReviews() throws Exception {
        List<Map<String, Object>> reviews = List.of(
                Map.of("reviewId", "rv_001", "movieId", "m_001", "userId", "user_001", "rating", 5, "comment", "Phim hay và cảm động, nên xem!", "createdAt", ts("2025-05-01T14:00:00Z"), "userFullName", "Nguyễn Văn A"),
                Map.of("reviewId", "rv_002", "movieId", "m_002", "userId", "user_002", "rating", 5, "comment", "Kỹ xảo mãn nhãn, nội dung hoành tráng.", "createdAt", ts("2025-03-10T20:00:00Z"), "userFullName", "Trần Văn B"),
                Map.of("reviewId", "rv_003", "movieId", "m_003", "userId", "user_003", "rating", 4.5, "comment", "Diễn xuất tốt, phim có chiều sâu.", "createdAt", ts("2025-02-15T19:00:00Z"), "userFullName", "Lê Thị C"),
                Map.of("reviewId", "rv_004", "movieId", "m_004", "userId", "user_005", "rating", 4, "comment", "Vui, xem giải trí ổn, có hơi nhiều cảm xúc mới.", "createdAt", ts("2025-06-20T16:00:00Z"), "userFullName", "Hoàng Thị E"),
                Map.of("reviewId", "rv_005", "movieId", "m_005", "userId", "user_004", "rating", 5, "comment", "Một kiệt tác của Christopher Nolan.", "createdAt", ts("2024-08-01T10:00:00Z"), "userFullName", "Phạm Văn D"),
                Map.of("reviewId", "rv_006", "movieId", "m_007", "userId", "user_001", "rating", 5, "comment", "Xem mà khóc quá trời, phim Thái làm hay thật.", "createdAt", ts("2025-06-10T22:00:00Z"), "userFullName", "Nguyễn Văn A"),
                Map.of("reviewId", "rv_007", "movieId", "m_002", "userId", "user_006", "rating", 4, "comment", "Phim hơi dài nhưng xem đáng tiền.", "createdAt", ts("2025-03-12T11:00:00Z"), "userFullName", "Võ Văn G"),
                Map.of("reviewId", "rv_008", "movieId", "m_010", "userId", "user_009", "rating", 3.5, "comment", "Phần này không hay bằng mấy phần trước.", "createdAt", ts("2025-03-15T18:00:00Z"), "userFullName", "Đỗ Thị L"),
                Map.of("reviewId", "rv_009", "movieId", "m_008", "userId", "user_008", "rating", 4, "comment", "Đánh nhau đã mắt, không cần suy nghĩ nhiều.", "createdAt", ts("2025-04-05T13:00:00Z"), "userFullName", "Bùi Hiển K"),
                Map.of("reviewId", "rv_010", "movieId", "m_001", "userId", "user_007", "rating", 4.5, "comment", "Cả nhà mình cùng đi xem, rất ý nghĩa.", "createdAt", ts("2025-05-02T17:00:00Z"), "userFullName", "Đặng Thị H")
        );
        System.out.println("... Seeding reviews ...");
        for (Map<String, Object> review : reviews) {
            db.collection("reviews").document((String) review.get("reviewId")).set(review).get();
        }
    }

    // 8. Thêm 10 Token thiết bị (device_tokens)
    private void seedDeviceTokens() throws Exception {
        List<Map<String, Object>> tokens = List.of(
                Map.of("userId", "user_001", "tokens", List.of("fcm_token_user_1_device_1...", "fcm_token_user_1_device_2...")),
                Map.of("userId", "user_002", "tokens", List.of("fcm_token_user_2_device_1...")),
                Map.of("userId", "user_003", "tokens", List.of("fcm_token_user_3_device_1...")),
                Map.of("userId", "user_004", "tokens", List.of("fcm_token_user_4_device_1...")),
                Map.of("userId", "user_005", "tokens", List.of("fcm_token_user_5_device_1...")),
                Map.of("userId", "user_006", "tokens", List.of("fcm_token_user_6_device_1...")),
                Map.of("userId", "user_007", "tokens", List.of("fcm_token_user_7_device_1...")),
                Map.of("userId", "user_008", "tokens", List.of("fcm_token_user_8_device_1...")),
                Map.of("userId", "user_009", "tokens", List.of("fcm_token_user_9_device_1...")),
                Map.of("userId", "user_010", "tokens", List.of("fcm_token_user_10_device_1..."))
        );
        System.out.println("... Seeding device_tokens ...");
        for (Map<String, Object> token : tokens) {
            // Dùng userId làm ID document
            db.collection("device_tokens").document((String) token.get("userId")).set(token).get();
        }
    }
}