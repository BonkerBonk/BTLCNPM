package com.btlcnpm.androidapp.data.model

// Model này phải khớp với Ticket.java của TicketService
data class Ticket(
    val ticketId: String,
    val bookingId: String,
    val userId: String,
    val qrCodeData: String, // Đây chính là ticketId
    val status: String,     // "VALID", "USED", "EXPIRED"
    val movieTitle: String,
    val theaterName: String,
    val roomName: String,
    val showtimeStartTime: FirestoreTimestamp? // Dùng lại model FirestoreTimestamp đã tạo
)