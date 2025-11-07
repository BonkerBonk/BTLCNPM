// PaymentService/src/main/java/com/btlcnpm/PaymentService/config/VnpayConfig.java
package com.btlcnpm.PaymentService.config;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jakarta.servlet.http.HttpServletRequest; // Dùng jakarta
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class VnpayConfig {

    // --- SAO CHÉP TỪ vnpay_jsp/src/java/com/vnpay/common/Config.java ---

    // Thay thế bằng thông tin thật của bạn
    public static String vnp_TmnCode = "YOUR_TMN_CODE";
    public static String vnp_HashSecret = "YOUR_HASH_SECRET";
    public static String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static String vnp_ReturnUrl = "http://localhost:8080/vnpay_return"; // App không dùng
    public static String vnp_ApiUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";

    // URL mà VNPay sẽ gọi (IPN) - trỏ về PaymentService
    public static String vnp_NotifyUrl = "http://YOUR_PUBLIC_IP:8092/api/v1/payment/vnpay-ipn";

    // Hàm băm HmacSHA512
    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    // Hàm lấy IP (cần HttpServletRequest)
    public static String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("X-FORWARDED-FOR");
            if (ipAdress == null) {
                ipAdress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAdress = "Invalid IP:" + e.getMessage();
        }
        return ipAdress;
    }

    // Hàm tạo chuỗi hash data
    public static String hashAllFields(Map<String, String> fields) {
        // Sắp xếp các trường theo thứ tự alphabet
        return fields.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }
}