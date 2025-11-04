package com.btlcnpm.TicketService.service;

import com.btlcnpm.TicketService.dto.BookingDTO;
import com.btlcnpm.TicketService.dto.DenormalizationDTOs;
import com.btlcnpm.TicketService.dto.TriggerTicketRequest;
import com.btlcnpm.TicketService.model.Ticket;
import com.btlcnpm.TicketService.repository.TicketRepository;
import com.google.cloud.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private RestTemplate restTemplate;

    // --- Định nghĩa URL của các service liên quan ---
    private final String BOOKING_SERVICE_URL = "http://localhost:8091/api/v1/booking/internal";
    private final String SHOWTIME_SERVICE_URL = "http://localhost:8089/api/v1/showtime/showtimes";
    private final String MOVIE_SERVICE_URL = "http://localhost:8085/api/v1/movies"; // Sửa lại URL cho đúng (movies)
    private final String THEATER_SERVICE_URL = "http://localhost:8086/api/v1/theaters"; // Sửa lại URL cho đúng (theaters)
    private final String ROOM_SERVICE_URL = "http://localhost:8088/api/v1/rooms"; // (Vẫn cần xác nhận API này)

    @Override
    public void createTicketsForBooking(TriggerTicketRequest request) throws Exception {

        // BƯỚC 1: Gọi BookingService (8091)
        String bookingApiUrl = BOOKING_SERVICE_URL + "/" + request.getBookingId();
        BookingDTO booking = restTemplate.getForObject(bookingApiUrl, BookingDTO.class);
        if (booking == null) throw new Exception("Booking không tồn tại.");


        // BƯỚC 2: LOGIC LẤY DỮ LIỆU THẬT
        String showtimeId = booking.getShowtimeId();
        DenormalizationDTOs.ShowtimeDTO showtime = null;
        DenormalizationDTOs.MovieDTO movie = null;
        DenormalizationDTOs.TheaterDTO theater = null;
        DenormalizationDTOs.RoomDTO room = null;

        try {
            // 1. Gọi ShowtimeService (8089)
            showtime = restTemplate.getForObject(SHOWTIME_SERVICE_URL + "/" + showtimeId, DenormalizationDTOs.ShowtimeDTO.class);
        } catch (Exception e) { System.err.println("Lỗi gọi Showtime: " + e.getMessage()); }

        if (showtime != null) {
            try {
                // 2. Gọi MovieService (8085)
                movie = restTemplate.getForObject(MOVIE_SERVICE_URL + "/" + showtime.getMovieId(), DenormalizationDTOs.MovieDTO.class);
            } catch (Exception e) { System.err.println("Lỗi gọi Movie: " + e.getMessage()); }

            try {
                // 3. Gọi TheaterService (8086)
                theater = restTemplate.getForObject(THEATER_SERVICE_URL + "/" + showtime.getTheaterId(), DenormalizationDTOs.TheaterDTO.class);
            } catch (Exception e) { System.err.println("Lỗi gọi Theater: " + e.getMessage()); }

            try {
                // 4. Gọi RoomService (8088)
                room = restTemplate.getForObject(ROOM_SERVICE_URL + "/" + showtime.getRoomId(), DenormalizationDTOs.RoomDTO.class);
            } catch (Exception e) { System.err.println("Lỗi gọi Room: " + e.getMessage()); }
        }

        // BƯỚC 3: TẠO VÉ (ĐÃ SỬA LỖI)

        // --- SỬA LỖI: Chuyển đổi String (từ DTO) sang Timestamp (cho Model) ---
        Timestamp showtimeTimestamp = Timestamp.now(); // Giá trị mặc định nếu lỗi
        if (showtime != null && showtime.getStartTime() != null) {
            try {
                // Dùng hàm parseTimestamp() để chuyển đổi chuỗi ISO String
                showtimeTimestamp = Timestamp.parseTimestamp(showtime.getStartTime());
            } catch (Exception e) {
                System.err.println("Lỗi parse (chuyển đổi) thời gian: " + e.getMessage());
            }
        }
        // --- KẾT THÚC SỬA LỖI ---

        for (int i = 0; i < booking.getQuantity(); i++) {
            Ticket newTicket = new Ticket();
            newTicket.setBookingId(request.getBookingId());
            newTicket.setUserId(request.getUserId());
            newTicket.setStatus("VALID");

            newTicket.setMovieTitle(movie != null ? movie.getTitle() : "Phim (Lỗi)");
            newTicket.setTheaterName(theater != null ? theater.getName() : "Rạp (Lỗi)");
            newTicket.setRoomName(room != null ? room.getName() : "Phòng (Lỗi)");
            newTicket.setShowtimeStartTime(showtimeTimestamp); // Gán Timestamp đã chuyển đổi

            ticketRepository.save(newTicket);
        }

        System.out.println("Đã tạo thành công " + booking.getQuantity() + " vé cho booking " + request.getBookingId());
    }

    @Override
    public List<Ticket> getMyTickets(String userId) throws Exception {

        List<Ticket> tickets = ticketRepository.getTicketsByUserId(userId);
        return tickets.stream()
                .filter(ticket -> "VALID".equals(ticket.getStatus()))
                .toList();
    }
}

