package com.btlcnpm.BookingService.service;

import com.btlcnpm.BookingService.dto.CreateBookingRequest;
import com.btlcnpm.BookingService.model.Booking;

import java.util.List;

public interface BookingService {
    Booking createBooking(String userId, CreateBookingRequest request) throws Exception;
    Booking getBookingById(String bookingId) throws Exception;
    List<Booking> getMyBookingHistory(String userId, String status) throws Exception;

    boolean updateBookingStatus(String bookingId, String newStatus) throws Exception;
}