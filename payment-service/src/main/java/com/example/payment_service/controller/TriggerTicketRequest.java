package com.example.payment_service.controller;

// DTO này dùng để gửi request sang TicketService
// (Nó phải khớp với DTO bên TicketService)
public class TriggerTicketRequest {
    private String bookingId;
    private String userId;

    // Constructors
    public TriggerTicketRequest() {}
    public TriggerTicketRequest(String bookingId, String userId) {
        this.bookingId = bookingId;
        this.userId = userId;
    }

    // Getters
    public String getBookingId() { return bookingId; }
    public String getUserId() { return userId; }

    // Setters
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public void setUserId(String userId) { this.userId = userId; }
}