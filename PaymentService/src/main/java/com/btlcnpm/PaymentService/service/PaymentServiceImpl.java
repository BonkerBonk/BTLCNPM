// PaymentService/src/main/java/com/btlcnpm/PaymentService/service/PaymentServiceImpl.java
package com.btlcnpm.PaymentService.service;

import com.btlcnpm.PaymentService.config.VnpayConfig; // <<< THÊM IMPORT NÀY
import com.btlcnpm.PaymentService.dto.CheckoutRequest;
import com.btlcnpm.PaymentService.dto.TriggerTicketRequest;
import com.btlcnpm.PaymentService.model.Booking;
import com.btlcnpm.PaymentService.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final String TICKET_SERVICE_URL = "http://localhost:8093/api/v1/ticket/internal/create";

    /**
     * Sửa đổi:
     * 1. Kiểu trả về là Map<String, Object>
     * 2. Thêm logic tạo VNPAY URL thật
     * 3. Xóa triggerTicketCreation() khỏi MOCK_SUCCESS
     */
    @Override
    public Map<String, Object> processPayment(CheckoutRequest request, String ipAddress) throws Exception {

        Booking booking = paymentRepository.getBookingById(request.getBookingId());
        if (booking == null) throw new Exception("Đơn hàng không tồn tại.");
        if (!"PENDING".equals(booking.getStatus())) throw new Exception("Đơn hàng đã được xử lý.");

        if ("MOMO_QR".equals(request.getPaymentMethod())) {
            // (Logic MoMo của bạn)
            System.out.println("Giả lập tạo QR Code cho MoMo...");
            String fakeQrCodeUrl = "https://me.momo.vn/pay/some-fake-qr-data-for-" + booking.getBookingId();
            return Map.of("paymentMethod", "MOMO_QR", "qrCodeUrl", fakeQrCodeUrl);

        } else if ("VNPAY".equals(request.getPaymentMethod())) {
            // === LOGIC VNPAY THẬT (dựa trên ajaxServlet.java) ===

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", VnpayConfig.vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf((long) booking.getTotalAmount() * 100));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", booking.getBookingId());
            vnp_Params.put("vnp_OrderInfo", "Thanh toan ve phim " + booking.getBookingId());
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", VnpayConfig.vnp_ReturnUrl); // App không dùng
            vnp_Params.put("vnp_IpAddr", ipAddress);
            vnp_Params.put("vnp_NotifyUrl", VnpayConfig.vnp_NotifyUrl); // IPN URL

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

            // 2. Tạo chữ ký
            String hashData = VnpayConfig.hashAllFields(vnp_Params);
            String secureHash = VnpayConfig.hmacSHA512(VnpayConfig.vnp_HashSecret, hashData);
            vnp_Params.put("vnp_SecureHash", secureHash);

            // 3. Build URL
            String queryUrl = vnp_Params.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII))
                    .collect(Collectors.joining("&"));

            String paymentUrl = VnpayConfig.vnp_Url + "?" + queryUrl;

            return Map.of("paymentMethod", "VNPAY", "payUrl", paymentUrl);

        } else if ("MOCK_SUCCESS".equals(request.getPaymentMethod())) {
            paymentRepository.updateBookingStatus(request.getBookingId(), "SUCCESSFUL");
            triggerTicketCreation(booking.getBookingId(), booking.getUserId());
            return Map.of("paymentMethod", "MOCK_SUCCESS", "message", "Thanh toán giả lập thành công.");
        } else {
            throw new Exception("Phương thức thanh toán không được hỗ trợ.");
        }
    }

    /**
     * Hàm này được gọi bởi IPN
     */
    @Override
    public void handleVnpayIpn(Map<String, String> ipnPayload) throws Exception {
        // 1. LẤY DỮ LIỆU TỪ PAYLOAD
        String vnp_SecureHash = ipnPayload.get("vnp_SecureHash");

        // Xóa vnp_SecureHash và vnp_SecureHashType khỏi map để kiểm tra chữ ký
        ipnPayload.remove("vnp_SecureHash");
        ipnPayload.remove("vnp_SecureHashType");

        // 2. TẠO CHỮ KÝ
        String hashData = VnpayConfig.hashAllFields(ipnPayload);
        String calculatedHash = VnpayConfig.hmacSHA512(VnpayConfig.vnp_HashSecret, hashData);

        // 3. SO SÁNH CHỮ KÝ
        if (!calculatedHash.equals(vnp_SecureHash)) {
            throw new Exception("Invalid Checksum (Sai chữ ký IPN)");
        }

        // 4. KIỂM TRA KẾT QUẢ THANH TOÁN
        String bookingId = ipnPayload.get("vnp_TxnRef");
        String responseCode = ipnPayload.get("vnp_ResponseCode");
        String transactionStatus = ipnPayload.get("vnp_TransactionStatus");

        Booking booking = paymentRepository.getBookingById(bookingId);
        if (booking == null) {
            throw new Exception("Order not found (Không tìm thấy đơn hàng)");
        }

        // Chỉ xử lý nếu đơn hàng đang PENDING
        if (!"PENDING".equals(booking.getStatus())) {
            throw new Exception("Order already confirmed (Đơn hàng đã được xử lý)");
        }

        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            // Thanh toán THÀNH CÔNG
            paymentRepository.updateBookingStatus(bookingId, "SUCCESSFUL");
            // KÍCH HOẠT TẠO VÉ
            triggerTicketCreation(bookingId, booking.getUserId());
            System.out.println("Đã xử lý IPN VNPay thành công cho booking: " + bookingId);
        } else {
            // Thanh toán THẤT BẠI
            paymentRepository.updateBookingStatus(bookingId, "FAILED");
            throw new Exception("Payment failed (Thanh toán thất bại)");
        }
    }

    // Hàm triggerTicketCreation giữ nguyên
    private void triggerTicketCreation(String bookingId, String userId) throws Exception {
        try {
            TriggerTicketRequest ticketRequest = new TriggerTicketRequest(
                    bookingId,
                    userId
            );
            restTemplate.postForObject(TICKET_SERVICE_URL, ticketRequest, String.class);
            System.out.println("Đã kích hoạt TicketService cho: " + bookingId);
        } catch (Exception e) {
            System.err.println("LỖI NGHIÊM TRỌNG: Không thể kích hoạt TicketService: " + e.getMessage());
            throw new Exception("Thanh toán thành công nhưng tạo vé thất bại: " + e.getMessage());
        }
    }

    // Thêm hàm rỗng cho processPayment cũ để tránh lỗi biên dịch
    @Override
    public String processPayment(CheckoutRequest request) throws Exception {
        // Hàm này sẽ không được dùng nữa, nhưng vẫn giữ để khớp interface
        return "Phương thức đã lỗi thời. Hãy dùng processPayment(request, ipAddress)";
    }
}