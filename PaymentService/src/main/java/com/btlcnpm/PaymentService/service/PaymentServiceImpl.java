package com.btlcnpm.PaymentService.service;

import com.btlcnpm.PaymentService.dto.SendPushRequest; // (DTO cho tương lai)
import com.btlcnpm.PaymentService.dto.TriggerTicketRequest;
import com.btlcnpm.PaymentService.dto.CheckoutRequest;
import com.btlcnpm.PaymentService.model.Booking;
import com.btlcnpm.PaymentService.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RestTemplate restTemplate;

    // URL của TicketService
    private final String TICKET_SERVICE_URL = "http://localhost:8093/api/v1/ticket/internal/create";
    // URL của PushNotificationService
    private final String PUSH_SERVICE_URL = "http://localhost:8095/api/v1/push/internal/send";

    @Override
    public String processPayment(CheckoutRequest request) throws Exception {

        // LẤY BOOKING TỪ DB
        Booking booking = paymentRepository.getBookingById(request.getBookingId());

        if (booking == null) {
            throw new Exception("Đơn hàng không tồn tại.");
        }

        // KIỂM TRA TRẠNG THÁI
        if (!"PENDING".equals(booking.getStatus())) {
            throw new Exception("Đơn hàng này đã được xử lý hoặc đã hết hạn.");
        }

        // GIẢ LẬP THANH TOÁN
        if (!"MOCK_SUCCESS".equals(request.getPaymentMethod())) {
            paymentRepository.updateBookingStatus(request.getBookingId(), "FAILED");
            throw new Exception("Thanh toán thất bại.");
        }

        // CẬP NHẬT STATUS
        paymentRepository.updateBookingStatus(request.getBookingId(), "SUCCESSFUL");



        try {
            TriggerTicketRequest ticketRequest = new TriggerTicketRequest(
                    booking.getBookingId(),
                    booking.getUserId()
            );
            // Gọi sang TicketService (8093) và yêu cầu nó tạo vé
            restTemplate.postForObject(TICKET_SERVICE_URL, ticketRequest, String.class);

        } catch (Exception e) {
            System.err.println("LỖI: Không thể kích hoạt TicketService: " + e.getMessage());
        }




        /*

        // GỌI PUSH NOTIFICATION SERVICE
        try {
            SendPushRequest pushRequest = new SendPushRequest(
                booking.getUserId(),
                "Thanh toán thành công!",
                "Cảm ơn bạn đã đặt vé. Mã booking của bạn là: " + booking.getBookingId()
            );
            restTemplate.postForObject(PUSH_SERVICE_URL, pushRequest, Void.class);
        } catch (Exception e) {
            System.err.println("LỖI: Không thể gửi Push Notification: " + e.getMessage());
        }

        */

        // Trả về thông báo thành công
        return "Thanh toán thành công.";
    }
}

