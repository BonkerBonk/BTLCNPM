package com.example.profile_service.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.text.ParseException; // <<< Cần cho Date Parsing
import java.text.SimpleDateFormat; // <<< Cần cho Date Formatting
import java.util.Date; // <<< Cần cho Date Object
import java.util.TimeZone; // <<< Cần cho chuẩn hóa múi giờ

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.profile_service.dto.UpdateProfileRequest;
import com.example.profile_service.dto.UserProfileResponse;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp; // <<< Dùng cho Firestore
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;

@Service
public class ProfileService {

    @Autowired
    private Firestore firestore; // Tiêm Firestore

    // Định dạng ngày tháng chuẩn cho API (YYYY-MM-DD)
    private static final String DATE_FORMAT = "yyyy-MM-dd";


    /**
     * Lấy thông tin hồ sơ dựa trên UID (lấy từ token)
     */
    public UserProfileResponse getMyProfile(String uid)
            throws ExecutionException, InterruptedException {

        // 1. Tạo yêu cầu truy vấn đến collection "users"
        ApiFuture<DocumentSnapshot> future = firestore.collection("users").document(uid).get();

        // 2. Lấy kết quả (chờ cho đến khi có)
        DocumentSnapshot document = future.get();

        // 3. Kiểm tra xem document có tồn tại không
        if (document.exists()) {
            // Lấy Timestamp từ Firestore
            Timestamp dobTimestamp = document.getTimestamp("dateOfBirth");
            String dobString = null; // Khởi tạo String rỗng/null

            // === FORMAT TIMESTAMP TO STRING (cho Frontend) ===
            if (dobTimestamp != null) {
                try {
                    // Định dạng và chuyển đổi Timestamp sang String (YYYY-MM-DD)
                    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Đảm bảo nhất quán
                    // Chuyển đổi Timestamp sang java.util.Date, rồi format thành String
                    dobString = sdf.format(dobTimestamp.toDate());
                } catch (Exception e) {
                    // Ghi log nếu lỗi format, nhưng vẫn trả về null
                    System.err.println("Error formatting dateOfBirth: " + e.getMessage());
                }
            }

            // 4. Nếu có, map dữ liệu sang DTO
            return UserProfileResponse.builder()
                    .userId(document.getString("userId"))
                    .email(document.getString("email"))
                    .fullName(document.getString("fullName"))
                    .phoneNumber(document.getString("phoneNumber"))
                    .profileImageUrl(document.getString("profileImageUrl"))
                    // Trả về String đã được format (dobString)
                    .dateOfBirth(dobString)
                    .build();
        } else {
            // 5. Nếu không tìm thấy user (ví dụ: lỗi đồng bộ)
            throw new RuntimeException("Không tìm thấy hồ sơ người dùng với UID: " + uid);
        }
    }

    /**
     * Cập nhật thông tin hồ sơ, bao gồm việc chuyển đổi String ngày sinh sang Timestamp.
     */
    public void updateMyProfile(String uid, UpdateProfileRequest request)
            throws ExecutionException, InterruptedException, IllegalArgumentException {

        // 1. Tạo một Map để chứa các trường cần cập nhật
        Map<String, Object> updates = new HashMap<>();

        // Helper để kiểm tra và lấy giá trị sạch (trim và null nếu rỗng)
        String fullName = (request.getFullName() != null) ? request.getFullName().trim() : null;
        String phoneNumber = (request.getPhoneNumber() != null) ? request.getPhoneNumber().trim() : null;
        String dateOfBirth = (request.getDateOfBirth() != null) ? request.getDateOfBirth().trim() : null;

        // 2. Cập nhật FullName
        if (fullName != null) {
            // Nếu chuỗi không rỗng/không chỉ chứa khoảng trắng, lưu giá trị đã trim
            updates.put("fullName", fullName.isBlank() ? null : fullName);
        }

        // 3. Cập nhật PhoneNumber
        if (phoneNumber != null) {
            // Nếu chuỗi không rỗng/không chỉ chứa khoảng trắng, lưu giá trị đã trim
            updates.put("phoneNumber", phoneNumber.isBlank() ? null : phoneNumber);
        }


        // 4. XỬ LÝ DATE OF BIRTH (String -> Timestamp)
        if (dateOfBirth != null) {
            if (dateOfBirth.isBlank()) {
                // Nếu gửi chuỗi rỗng/chỉ khoảng trắng -> set null trong Firestore
                updates.put("dateOfBirth", null);
            } else {
                try {
                    // Định dạng phải khớp với chuỗi gửi lên (YYYY-MM-DD)
                    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Chuẩn hóa múi giờ

                    // Parse chuỗi ngày sinh thành đối tượng Date
                    Date parsedDate = sdf.parse(dateOfBirth);
                    // Chuyển đổi Date thành Firestore Timestamp
                    Timestamp dobTimestamp = Timestamp.of(parsedDate);

                    updates.put("dateOfBirth", dobTimestamp); // Thêm Timestamp vào Map
                } catch (ParseException e) {
                    // Ném lỗi nếu client gửi định dạng ngày sai (sẽ bị bắt và trả về 400 Bad Request)
                    throw new IllegalArgumentException("Định dạng ngày sinh không hợp lệ. Vui lòng sử dụng YYYY-MM-DD.", e);
                }
            }
        }
        // === KẾT THÚC XỬ LÝ DATE OF BIRTH ===

        // 5. Kiểm tra xem có gì để cập nhật không
        if (updates.isEmpty()) {
            return;
        }

        // 6. Gửi lệnh "update" lên Firestore
        ApiFuture<WriteResult> future = firestore.collection("users").document(uid).update(updates);
        // Chờ cập nhật xong
        future.get();
    }
}