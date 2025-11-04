package com.btlcnpm.BookingService.service;

import com.btlcnpm.BookingService.dto.CreateBookingRequest;
import com.btlcnpm.BookingService.dto.ShowtimeDTO;
import com.btlcnpm.BookingService.dto.UpdateTicketsRequest;
import com.btlcnpm.BookingService.model.Booking;
import com.btlcnpm.BookingService.repository.BookingRepository;
import com.google.cloud.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RestTemplate restTemplate;

    // URL của ShowtimeService
    private final String SHOWTIME_SERVICE_URL = "http://localhost:8089/api/v1/showtime";

    @Override
    public Booking createBooking(String userId, CreateBookingRequest request) throws Exception {

//        // KIỂM TRA VÉ (LOGIC HIỆN TẠI ĐANG MOCK)
//
//        double ticketPrice = 75000; // Giả lập giá vé
//        int availableTickets = 100; // Giả lập số vé còn


        // CHECK VÉ
        ShowtimeDTO showtime;
        String getShowtimeApiUrl = SHOWTIME_SERVICE_URL + "/showtimes/" + request.getShowtimeId();

        try {
            showtime = restTemplate.getForObject(getShowtimeApiUrl, ShowtimeDTO.class);
            if (showtime == null) {
                throw new Exception("Không tìm thấy suất chiếu (Showtime not found).");
            }
        } catch (HttpClientErrorException e) {
             throw new Exception("Lỗi khi gọi ShowtimeService: " + e.getMessage());
        }

        double ticketPrice = showtime.getTicketPrice();
        int availableTickets = showtime.getAvailableTickets();



        // KIỂM TRA LOGIC
        if (request.getQuantity() <= 0) {
            throw new Exception("Số lượng vé phải lớn hơn 0");
        }

        if (request.getQuantity() > availableTickets) {
            // Trả về lỗi 400 theo đề cương
            throw new Exception("Không đủ vé.");
        }

        // TẠO BOOKING
        double totalAmount = ticketPrice * request.getQuantity();

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setShowtimeId(request.getShowtimeId());
        booking.setQuantity(request.getQuantity());
        booking.setTotalAmount(totalAmount);
        booking.setStatus("PENDING"); // Trạng thái PENDING theo đề cương
        booking.setCreatedAt(Timestamp.now()); //

        // LƯU VÀO DB -----
        Booking savedBooking = bookingRepository.save(booking);


        String updateTicketsApiUrl = SHOWTIME_SERVICE_URL + "/internal/update-tickets";
        UpdateTicketsRequest updateRequest = new UpdateTicketsRequest(
                request.getShowtimeId(),
                -request.getQuantity() // Gửi số âm (-2) để trừ vé
        );

        try {
            restTemplate.postForObject(updateTicketsApiUrl, updateRequest, Void.class);
        } catch (Exception e) {
            System.err.println("LỖI NGHIÊM TRỌNG: Đã tạo booking nhưng không thể trừ vé: " + e.getMessage());
        }



        return savedBooking;
    }

    @Override
    public Booking getBookingById(String bookingId) throws Exception {

        return bookingRepository.getBookingById(bookingId);
    }

    @Override
    public List<Booking> getMyBookingHistory(String userId, String status) throws Exception {

        return bookingRepository.getBookingsByUserIdAndStatus(userId, status);
    }
}