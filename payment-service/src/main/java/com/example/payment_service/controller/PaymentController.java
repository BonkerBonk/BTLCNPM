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

    private final String BOOKING_SERVICE_URL = "http://localhost:8091/api/v1/booking/internal/";
    private final String TICKET_SERVICE_URL = "http://localhost:8093/api/v1/ticket/internal/create";

    @PostMapping("/checkout")
    public ResponseEntity<?> createPayment(
            @RequestBody CheckoutRequest request,
            HttpServletRequest httpServletRequest
    ) throws UnsupportedEncodingException {

        String bookingId = request.getBookingId();
        System.out.println("===== CHECKOUT REQUEST =====");
        System.out.println("BookingId: " + bookingId);

        // === BƯỚC 1: LẤY SỐ TIỀN TỪ BOOKING SERVICE ===
        long amount;
        BookingDTO booking;
        try {
            String url = BOOKING_SERVICE_URL + bookingId;
            System.out.println("Calling Booking Service: " + url);

            booking = restTemplate.getForObject(url, BookingDTO.class);

            if (booking == null) {
                System.err.println("ERROR: Booking not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Không tìm thấy đơn hàng: " + bookingId));
            }

            amount = (long) booking.getTotalAmount() * 100;
            System.out.println("Booking found - Amount: " + amount);

        } catch (Exception e) {
            System.err.println("ERROR calling Booking Service: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy thông tin đơn hàng: " + e.getMessage()));
        }

        // === BƯỚC 2: TẠO URL THANH TOÁN ===
        try {
            String orderType = "other";
            String bankCode = "NCB";
            String vnp_TxnRef = bookingId;
            String vnp_IpAddr = ConfigVNpay.getIpAddress(httpServletRequest);
            String vnp_TmnCode = ConfigVNpay.vnp_TmnCode;

            System.out.println("Creating VNPay URL with:");
            System.out.println("- TxnRef: " + vnp_TxnRef);
            System.out.println("- Amount: " + amount);
            System.out.println("- IP: " + vnp_IpAddr);

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

            System.out.println("===== PAYMENT URL CREATED =====");
            System.out.println("Full URL length: " + paymentUrl.length());
            System.out.println("URL: " + paymentUrl);
            System.out.println("===============================");

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("payUrl", paymentUrl);
            responseMap.put("status", "ok");
            responseMap.put("message", "success");
            responseMap.put("bookingId", bookingId);

            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            System.err.println("ERROR creating payment URL: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi tạo URL thanh toán: " + e.getMessage()));
        }
    }

    @GetMapping("/payment-callback")
    public ResponseEntity<?> paymentCallback(@RequestParam Map<String, String> queryParams) {
        String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
        String bookingId = queryParams.get("vnp_TxnRef");

        System.out.println("===== PAYMENT CALLBACK =====");
        System.out.println("Response Code: " + vnp_ResponseCode);
        System.out.println("BookingId: " + bookingId);
        System.out.println("============================");

        if ("00".equals(vnp_ResponseCode)) {
            try {
                // BƯỚC 1: Lấy thông tin booking
                String url = BOOKING_SERVICE_URL + bookingId;
                System.out.println("📞 Gọi GET: " + url);

                BookingDTO booking = restTemplate.getForObject(url, BookingDTO.class);

                if (booking == null || booking.getUserId() == null) {
                    throw new Exception("Không tìm thấy booking hoặc userId.");
                }

                System.out.println("✅ Lấy booking thành công, userId: " + booking.getUserId());

                // BƯỚC 2: Tạo vé
                System.out.println("📞 Gọi POST: " + TICKET_SERVICE_URL);
                TriggerTicketRequest ticketRequest = new TriggerTicketRequest(bookingId, booking.getUserId());
                Map<String, Object> ticketResponse = restTemplate.postForObject(
                        TICKET_SERVICE_URL,
                        ticketRequest,
                        Map.class
                );
                System.out.println("✅ Tạo vé thành công: " + ticketResponse);

                // BƯỚC 3: Cập nhật trạng thái booking
                try {
                    String updateUrl = BOOKING_SERVICE_URL + bookingId + "/status";
                    System.out.println("📞 Gọi PUT: " + updateUrl);
                    System.out.println("   Body: {\"status\": \"SUCCESSFUL\"}");

                    restTemplate.put(
                            updateUrl,
                            Map.of("status", "SUCCESSFUL")
                    );

                    System.out.println("✅ Cập nhật booking status thành công");

                } catch (Exception e) {
                    System.err.println("❌ Lỗi cập nhật status booking: " + e.getMessage());
                    e.printStackTrace();
                    // Không throw exception để vẫn trả về success cho VNPay
                }

                return ResponseEntity.ok(Map.of("message", "Thanh toán thành công, vé đang được xử lý."));

            } catch (Exception e) {
                System.err.println("❌ Lỗi trong callback: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "Lỗi server: " + e.getMessage()));
            }
        } else {
            // Thanh toán thất bại
            System.out.println("❌ Thanh toán thất bại với mã: " + vnp_ResponseCode);

            try {
                String updateUrl = BOOKING_SERVICE_URL + bookingId + "/status";
                System.out.println("📞 Cập nhật booking sang FAILED: " + updateUrl);

                restTemplate.put(
                        updateUrl,
                        Map.of("status", "FAILED")
                );

                System.out.println("✅ Đã cập nhật booking sang FAILED");

            } catch (Exception e) {
                System.err.println("❌ Không thể cập nhật status sang FAILED: " + e.getMessage());
            }

            return ResponseEntity.badRequest().body(Map.of("message", "Thanh toán thất bại"));
        }
    }
}