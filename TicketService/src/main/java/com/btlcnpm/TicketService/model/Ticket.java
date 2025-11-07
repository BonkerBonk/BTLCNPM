package com.btlcnpm.TicketService.model;

import com.google.cloud.Timestamp;
import lombok.Data;

@Data
public class Ticket {

    private String ticketId;
    private String bookingId;
    private String userId;
    private String qrCodeData; // ticketId
    private String status;     // "VALID", "USED", "EXPIRED"
    private String movieTitle;
    private String theaterName;
    private String roomName;
    private Timestamp showtimeStartTime;
}