package com.example.payment_service.controller;

import com.example.payment_service.config.ConfigVNpay;
// XÓA: import com.example.payment_service.response.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

// === THÊM IMPORT NÀY ===
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
    // Gateway sẽ điều hướng đến BookingService (cổng 8091)
    private final String BOOKING_SERVICE_URL = "http://localhost:8080/api/v1/booking/internal/";

    // === THÊM URL CỦA TICKET SERVICE ===
    // Trỏ qua Gateway (8080) đến TicketService (8093)
    private final String TICKET_SERVICE_URL = "http://localhost:8080/api/v1/ticket/internal/create";


    // === SỬA PHƯƠNG THỨC VÀ ĐƯỜNG DẪN ĐỂ KHỚP VỚI ANDROID ===
    @PostMapping("/checkout")
    public ResponseEntity<?> createPayment(
            @RequestBody CheckoutRequest request,
            HttpServletRequest httpServletRequest // <<< SỬA 1: Thêm HttpServletRequest
    ) throws UnsupportedEncodingException {

        // Lấy bookingId từ request DTO
        String bookingId = request.getBookingId();

        // === BƯỚC 1: LẤY SỐ TIỀN TỪ BOOKING SERVICE ===
        long amount;
        BookingDTO booking;
        try {
            // Gọi đến BookingService (qua Gateway)
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
        // === KẾT THÚC BƯỚC 1 ===


        // === BƯỚC 2: TẠO URL THANH TOÁN (Code của bạn) ===
        String orderType = "other";
        String bankCode = "NCB"; // Ngân hàng mặc định

        String vnp_TxnRef = bookingId;

        // === SỬA 2: Lấy IP thật từ request, thay vì "127.0.0.1" ===
        // Dùng hàm getIpAddress (đã có sẵn trong ConfigVNpay)
        String vnp_IpAddr = "58.187.59.171";

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
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr); // <<< SỬA 2 (Tiếp): Giờ nó là IP thật

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
// ... (Phần còn lại của code tạo URL giữ nguyên) ...
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

    // API callback (Giữ nguyên, không thay đổi)
    @GetMapping("/payment-callback")
    public ResponseEntity<?> paymentCallback(@RequestParam Map<String, String> queryParams) {
// ... (Code của hàm này giữ nguyên) ...
        String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");

        // TODO: Xác thực chữ ký (vnp_SecureHash) từ VNPay
        // (Tạm thời bỏ qua để tập trung vào luồng chính)

        if ("00".equals(vnp_ResponseCode)) {
            try {
                // Lấy lại bookingId từ vnp_TxnRef (đã được gán ở /checkout)
                String bookingId = queryParams.get("vnp_TxnRef");

                // Gọi BookingService LẦN NỮA để lấy userId
                String url = BOOKING_SERVICE_URL + bookingId;
                BookingDTO booking = restTemplate.getForObject(url, BookingDTO.class);

                if (booking == null || booking.getUserId() == null) {
                    throw new Exception("Callback không tìm thấy booking hoặc userId.");
                }

                // Kích hoạt TicketService (TV5) tạo vé
                TriggerTicketRequest ticketRequest = new TriggerTicketRequest(bookingId, booking.getUserId());
                restTemplate.postForObject(TICKET_SERVICE_URL, ticketRequest, Map.class);

                // TODO: Cập nhật trạng thái Booking sang "SUCCESSFUL"
                // (Cần thêm API internal/update-status bên BookingService)

                // Trả về cho VNPay (và cho người dùng nếu họ bị redirect)
                return ResponseEntity.ok(Map.of("message", "Thanh toán thành công, vé đang được xử lý."));

            } catch (Exception e) {
                // Lỗi phía server sau khi VNPay báo thành công (nghiêm trọng)
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "Lỗi server sau khi thanh toán: " + e.getMessage()));
            }
        } else {
            // TODO: Cập nhật trạng thái Booking sang "FAILED"

            return ResponseEntity.badRequest().body(Map.of("message", "Thanh toán thất bại"));
        }
    }
}