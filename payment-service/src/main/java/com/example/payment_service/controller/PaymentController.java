package com.example.payment_service.controller;

import com.example.payment_service.config.ConfigVNpay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.example.payment_service.config.ConfigVNpay.vnp_Command;
import static com.example.payment_service.config.ConfigVNpay.vnp_Version;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {

    @Autowired
    private RestTemplate restTemplate;

    // URL này trỏ đến API Gateway (cổng 8080)
    private final String BOOKING_SERVICE_URL = "http://localhost:8080/api/v1/booking/internal/";
    private final String TICKET_SERVICE_URL = "http://localhost:8080/api/v1/ticket/internal/create";


    @PostMapping("/checkout")
    public ResponseEntity<?> createPayment(
            @RequestBody CheckoutRequest request,
            HttpServletRequest httpServletRequest
    ) throws UnsupportedEncodingException {

        String bookingId = request.getBookingId();

        // === BƯỚC 1: LẤY SỐ TIỀN TỪ BOOKING SERVICE ===
        long amount;
        BookingDTO booking;
        try {
            String url = BOOKING_SERVICE_URL + bookingId;
            booking = restTemplate.getForObject(url, BookingDTO.class);

            if (booking == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Không tìm thấy đơn hàng: " + bookingId));
            }
            amount = (long) booking.getTotalAmount() * 100;

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy thông tin đơn hàng: " + e.getMessage()));
        }

        // === BƯỚC 2: TẠO URL THANH TOÁN ===
        String orderType = "other";
        String bankCode = "NCB";
        String vnp_TxnRef = bookingId;
        String vnp_IpAddr = ConfigVNpay.getIpAddress(httpServletRequest);
        String vnp_TmnCode = ConfigVNpay.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", bankCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", ConfigVNpay.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = ConfigVNpay.hmacSHA512(ConfigVNpay.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = ConfigVNpay.vnp_PayUrl + "?" + queryUrl;

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("payUrl", paymentUrl);
        responseMap.put("status", "ok");
        responseMap.put("message", "success");
        responseMap.put("bookingId", bookingId);

        return ResponseEntity.ok(responseMap);
    }

    @GetMapping("/payment-callback")
    public ResponseEntity<?> paymentCallback(@RequestParam Map<String, String> queryParams) {
        String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
        String bookingId = queryParams.get("vnp_TxnRef");

        // TODO: Xác thực chữ ký (vnp_SecureHash) từ VNPay

        if ("00".equals(vnp_ResponseCode)) {
            // ===== THANH TOÁN THÀNH CÔNG =====
            try {
                // Lấy userId từ BookingService
                String url = BOOKING_SERVICE_URL + bookingId;
                BookingDTO booking = restTemplate.getForObject(url, BookingDTO.class);

                if (booking == null || booking.getUserId() == null) {
                    throw new Exception("Callback không tìm thấy booking hoặc userId.");
                }

                // Kích hoạt TicketService tạo vé
                TriggerTicketRequest ticketRequest = new TriggerTicketRequest(bookingId, booking.getUserId());
                restTemplate.postForObject(TICKET_SERVICE_URL, ticketRequest, Map.class);

                // ===== CẬP NHẬT TRẠNG THÁI BOOKING SANG "SUCCESSFUL" =====
                try {
                    restTemplate.put(
                            BOOKING_SERVICE_URL + bookingId + "/status",
                            Map.of("status", "SUCCESSFUL"),
                            Void.class
                    );
                } catch (Exception e) {
                    System.err.println("CẢNH BÁO: Không thể cập nhật status booking: " + e.getMessage());
                }

                return ResponseEntity.ok(Map.of("message", "Thanh toán thành công, vé đang được xử lý."));

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "Lỗi server sau khi thanh toán: " + e.getMessage()));
            }
        } else {
            // ===== THANH TOÁN THẤT BẠI =====
            try {
                restTemplate.put(
                        BOOKING_SERVICE_URL + bookingId + "/status",
                        Map.of("status", "FAILED"),
                        Void.class
                );
            } catch (Exception e) {
                System.err.println("CẢNH BÁO: Không thể cập nhật status booking sang FAILED: " + e.getMessage());
            }

            return ResponseEntity.badRequest().body(Map.of("message", "Thanh toán thất bại"));
        }
    }
}