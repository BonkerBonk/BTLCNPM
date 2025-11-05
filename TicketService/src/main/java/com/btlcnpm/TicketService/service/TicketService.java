package com.btlcnpm.TicketService.service;

import com.btlcnpm.TicketService.dto.TriggerTicketRequest;
import com.btlcnpm.TicketService.model.Ticket;

import java.util.List;

public interface TicketService {


//    Tạo vé cho một đơn hàng đã thanh toán
    void createTicketsForBooking(TriggerTicketRequest request) throws Exception;

//    Lấy tất cả vé của một người dùng
    List<Ticket> getMyTickets(String userId) throws Exception;
}