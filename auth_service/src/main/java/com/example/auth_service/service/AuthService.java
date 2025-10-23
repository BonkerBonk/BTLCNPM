package com.example.auth_service.service;

import com.example.auth_service.dto.LoginRequest; // MỚI
import com.example.auth_service.dto.LoginResponse; // MỚI
import com.example.auth_service.dto.RegisterRequest;
import com.example.auth_service.dto.RegisterResponse;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // MỚI
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException; // MỚI
import org.springframework.web.client.RestTemplate; // MỚI

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class AuthService {

    @Autowired
    private FirebaseAuth firebaseAuth;

    @Autowired
    private Firestore firestore;

    @Autowired
    private RestTemplate restTemplate; // MỚI: Tiêm công cụ gọi API

    @Value("${firebase.web-api-key}") // MỚI: Đọc key từ application.properties
    private String firebaseApiKey;

    // --- HÀM REGISTER (Giữ nguyên) ---
    public RegisterResponse register(RegisterRequest request)
            throws FirebaseAuthException, ExecutionException, InterruptedException {

        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setEmail(request.getEmail())
                .setPassword(request.getPassword())
                .setDisplayName(request.getFullName());

        UserRecord userRecord;
        try {
            userRecord = firebaseAuth.createUser(createRequest);
        } catch (FirebaseAuthException e) {
            if (e.getAuthErrorCode() == com.google.firebase.auth.AuthErrorCode.EMAIL_ALREADY_EXISTS) {
                throw new RuntimeException("Email đã tồn tại.");
            }
            throw e;
        }

        String uid = userRecord.getUid();
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", uid);
        userData.put("email", request.getEmail());
        userData.put("fullName", request.getFullName());
        userData.put("phoneNumber", "");
        userData.put("profileImageUrl", "");
        userData.put("createdAt", com.google.cloud.Timestamp.now());

        ApiFuture<WriteResult> future = firestore.collection("users").document(uid).set(userData);
        future.get();

        return new RegisterResponse(uid, userRecord.getEmail(), userRecord.getDisplayName());
    }

    // --- HÀM LOGIN (MỚI) ---
    public LoginResponse login(LoginRequest request) {
        // 1. Chuẩn bị URL
        // Đây là API đăng nhập bằng email/pass của Google
        String loginUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + firebaseApiKey;

        // 2. Chuẩn bị Request Body
        // Google yêu cầu 3 trường này
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", request.getEmail());
        requestBody.put("password", request.getPassword());
        requestBody.put("returnSecureToken", true);

        try {
            // 3. Gọi API
            // Gửi 1 POST request đến loginUrl, mang theo requestBody,
            // và mong đợi nhận về 1 Map
            Map<String, Object> response = restTemplate.postForObject(loginUrl, requestBody, Map.class);

            if (response != null && response.containsKey("idToken")) {
                // 4. Lấy kết quả
                String token = (String) response.get("idToken"); // Đây là JWT Token
                String userId = (String) response.get("localId"); // Đây là UID (userId)

                return new LoginResponse(userId, token);
            } else {
                throw new RuntimeException("Đăng nhập thất bại: Không nhận được token.");
            }
        } catch (HttpClientErrorException e) {
            // 5. Bắt lỗi (Sai mật khẩu / email không tồn tại)
            // Firebase trả về lỗi 400
            // Bạn có thể log e.getResponseBodyAsString() để xem chi tiết lỗi
            throw new RuntimeException("Email hoặc mật khẩu không chính xác.");
        }
    }
    // --- HÀM FORGOT PASSWORD (MỚI) ---
    public String forgotPassword(String email) {
        try {
            // 1. Gọi Firebase Auth để tạo link reset
            String link = firebaseAuth.generatePasswordResetLink(email);

            // 2. Gửi email (Quan trọng!)
            // TODO: Ở đây, bạn cần gọi EmailService (Service 14)
            // để gửi email chứa 'link' này cho người dùng.
            // Tạm thời chúng ta sẽ in ra console để test
            System.out.println("Generated password reset link: " + link);

            // 3. Trả về thông báo thành công
            return "Email reset mật khẩu đã được gửi.";
            
        } catch (FirebaseAuthException e) {
            // Bắt lỗi nếu email không tồn tại
            if (e.getAuthErrorCode() == com.google.firebase.auth.AuthErrorCode.EMAIL_NOT_FOUND) {
                // Vẫn trả về thông báo thành công để bảo mật
                // (Không cho hacker biết email nào có tồn tại)
                return "Email reset mật khẩu đã được gửi.";
            }
            throw new RuntimeException("Lỗi khi tạo link reset: " + e.getMessage());
        }
    }
}