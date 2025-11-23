package com.btlcnpm.BookingService.service;

import com.btlcnpm.BookingService.dto.CreateBookingRequest;
import com.btlcnpm.BookingService.dto.ShowtimeDTO;
import com.btlcnpm.BookingService.dto.UpdateTicketsRequest;
import com.btlcnpm.BookingService.model.Booking;
import com.btlcnpm.BookingService.repository.BookingRepository;
import com.google.cloud.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

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
            throw new Exception("Không đủ vé.");
        }

        // TẠO BOOKING
        double totalAmount = ticketPrice * request.getQuantity();

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setShowtimeId(request.getShowtimeId());
        booking.setQuantity(request.getQuantity());
        booking.setTotalAmount(totalAmount);
        booking.setStatus("PENDING");
        booking.setCreatedAt(Timestamp.now());

        // ===== ĐÃ SỬA: TRỪ VÉ TRƯỚC KHI LƯU BOOKING =====
        String updateTicketsApiUrl = SHOWTIME_SERVICE_URL + "/internal/update-tickets";
        UpdateTicketsRequest updateRequest = new UpdateTicketsRequest(
                request.getShowtimeId(),
                -request.getQuantity()
        );

        try {
            restTemplate.postForObject(updateTicketsApiUrl, updateRequest, Void.class);
        } catch (Exception e) {
            // Nếu không trừ được vé, throw exception để không tạo booking
            throw new Exception("Không thể trừ vé từ ShowtimeService: " + e.getMessage());
        }

        // CHỈ LƯU BOOKING SAU KHI TRỪ VÉ THÀNH CÔNG
        Booking savedBooking = bookingRepository.save(booking);

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

    /**
     * ===== IMPLEMENT: CẬP NHẬT TRẠNG THÁI BOOKING =====
     */
    @Override
    public boolean updateBookingStatus(String bookingId, String newStatus) throws Exception {
        Booking booking = bookingRepository.getBookingById(bookingId);

        if (booking == null) {
            return false;
        }

        // ===== BỔ SUNG: HOÀN VÉ KHI THANH TOÁN THẤT BẠI =====
        if ("FAILED".equals(newStatus) && "PENDING".equals(booking.getStatus())) {
            // Gọi ShowtimeService để hoàn lại vé
            try {
                String rollbackUrl = SHOWTIME_SERVICE_URL + "/internal/rollback-tickets";
                Map<String, Object> rollbackRequest = Map.of(
                        "showtimeId", booking.getShowtimeId(),
                        "quantity", booking.getQuantity()
                );
                restTemplate.postForObject(rollbackUrl, rollbackRequest, Void.class);
            } catch (Exception e) {
                System.err.println("CẢNH BÁO: Không thể rollback vé: " + e.getMessage());
                // Không throw exception vì trạng thái vẫn cần được cập nhật
            }
        }

        // Cập nhật status
        return bookingRepository.updateStatus(bookingId, newStatus);
    }
}