package com.example.profile_service.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.profile_service.dto.UpdateProfileRequest;
import com.example.profile_service.dto.UserProfileResponse;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;

@Service
public class ProfileService {

    @Autowired
    private Firestore firestore; // Tiêm Firestore

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
            // 4. Nếu có, map dữ liệu sang DTO
            // Dùng @Builder mà chúng ta đã tạo trong DTO
            return UserProfileResponse.builder()
                    .userId(document.getString("userId"))
                    .email(document.getString("email"))
                    .fullName(document.getString("fullName"))
                    .phoneNumber(document.getString("phoneNumber"))
                    .profileImageUrl(document.getString("profileImageUrl"))
                    .dateOfBirth(document.getTimestamp("dateOfBirth"))
                    .build();
        } else {
            // 5. Nếu không tìm thấy user (ví dụ: lỗi đồng bộ)
            throw new RuntimeException("Không tìm thấy hồ sơ người dùng với UID: " + uid);
        }
    }

public void updateMyProfile(String uid, UpdateProfileRequest request) 
        throws ExecutionException, InterruptedException {
        
    // 1. Tạo một Map để chứa các trường cần cập nhật
    // Chúng ta dùng Map thay vì đối tượng để chỉ cập nhật
    // các trường được gửi lên, không ghi đè các trường khác.
    Map<String, Object> updates = new HashMap<>();

    // 2. Kiểm tra xem trường nào được gửi lên thì mới thêm vào Map
    if (request.getFullName() != null) {
        updates.put("fullName", request.getFullName());
    }
    if (request.getPhoneNumber() != null) {
        updates.put("phoneNumber", request.getPhoneNumber());
    }
    if (request.getDateOfBirth() != null) {
        updates.put("dateOfBirth", request.getDateOfBirth());
    }

    // 3. Kiểm tra xem có gì để cập nhật không
    if (updates.isEmpty()) {
        // Không có gì để làm, chỉ cần trả về
        return; 
    }

    // 4. Gửi lệnh "update" lên Firestore
    // Dùng .update() thay vì .set() để không ghi đè toàn bộ document
    ApiFuture<WriteResult> future = firestore.collection("users").document(uid).update(updates);

     // 5. Chờ cho đến khi cập nhật xong
    future.get();
    }
    // (Chúng ta sẽ thêm hàm updateMyProfile vào đây sau)
}