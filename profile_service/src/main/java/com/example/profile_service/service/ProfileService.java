package com.example.profile_service.service;
import java.text.ParseException; // <<< Import ParseException
import java.text.SimpleDateFormat; // <<< Import SimpleDateFormat
import java.util.Date;             // <<< Import Date
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;         // <<< Import TimeZone
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.profile_service.dto.UpdateProfileRequest;
import com.example.profile_service.dto.UserProfileResponse;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp; // <<< Import Timestamp
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
            // Lấy Timestamp từ Firestore
            Timestamp dobTimestamp = document.getTimestamp("dateOfBirth");
            String dobString = null; // Khởi tạo là null

            // Kiểm tra xem Timestamp có tồn tại không
            if (dobTimestamp != null) {
                // Định dạng mong muốn (ví dụ: YYYY-MM-DD)
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Đảm bảo đúng múi giờ
                // Format Timestamp thành String
                dobString = sdf.format(dobTimestamp.toDate());
            }
            return UserProfileResponse.builder()
                    .userId(document.getString("userId"))
                    .email(document.getString("email"))
                    .fullName(document.getString("fullName"))
                    .phoneNumber(document.getString("phoneNumber"))
                    .profileImageUrl(document.getString("profileImageUrl"))
                    .dateOfBirth(dobString)
                    .build();
        } else {
            // 5. Nếu không tìm thấy user (ví dụ: lỗi đồng bộ)
            throw new RuntimeException("Không tìm thấy hồ sơ người dùng với UID: " + uid);
        }
    }

public void updateMyProfile(String uid, UpdateProfileRequest request) 
        throws ExecutionException, InterruptedException, IllegalArgumentException {
        
    // 1. Tạo một Map để chứa các trường cần cập nhật
    // Chúng ta dùng Map thay vì đối tượng để chỉ cập nhật
    // các trường được gửi lên, không ghi đè các trường khác.
    Map<String, Object> updates = new HashMap<>();

    // 2. Kiểm tra xem trường nào được gửi lên thì mới thêm vào Map
    if (request.getFullName() != null && !request.getFullName().isBlank()) { // Thêm check isBlank
        updates.put("fullName", request.getFullName().trim()); // Thêm trim()
    }
    if (request.getPhoneNumber() != null) {
        // Nếu gửi chuỗi rỗng thì coi như muốn xóa (lưu null), nếu không thì trim
        updates.put("phoneNumber", request.getPhoneNumber().isBlank() ? null : request.getPhoneNumber().trim());
    }
    // === XỬ LÝ DATE OF BIRTH ===
    if (request.getDateOfBirth() != null) {
        if (request.getDateOfBirth().isBlank()) {
            // Nếu gửi chuỗi rỗng -> muốn xóa ngày sinh
            updates.put("dateOfBirth", null);
        } else {
            try {
                // Định dạng phải khớp với chuỗi gửi lên (ví dụ: YYYY-MM-DD)
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                // Quan trọng: Đặt múi giờ UTC để lưu trữ nhất quán
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                // Parse chuỗi thành đối tượng Date
                Date parsedDate = sdf.parse(request.getDateOfBirth().trim());
                // Chuyển đổi Date thành Firestore Timestamp
                Timestamp dobTimestamp = Timestamp.of(parsedDate);
                // Đưa Timestamp vào map để cập nhật
                updates.put("dateOfBirth", dobTimestamp);
            } catch (ParseException e) {
                // Ném lỗi nếu người dùng gửi định dạng ngày sai
                throw new IllegalArgumentException("Định dạng ngày sinh không hợp lệ. Vui lòng sử dụng YYYY-MM-DD.", e);
            }
        }
    }
    // === KẾT THÚC XỬ LÝ DATE OF BIRTH ===

    // 3. Kiểm tra xem có gì để cập nhật không
    if (updates.isEmpty()) {
        System.out.println("Không có trường nào cần cập nhật cho user: " + uid); // Thêm log
        return; 
    }

    // 4. Gửi lệnh "update" lên Firestore
    // Dùng .update() thay vì .set() để không ghi đè toàn bộ document
    System.out.println("Đang cập nhật user " + uid + " với dữ liệu: " + updates); // Thêm log
    ApiFuture<WriteResult> future = firestore.collection("users").document(uid).update(updates);

    WriteResult result = future.get();
    System.out.println("Cập nhật thành công user " + uid + " lúc: " + result.getUpdateTime()); // Thêm log
     // 5. Chờ cho đến khi cập nhật xong
    future.get();
    }
    // (Chúng ta sẽ thêm hàm updateMyProfile vào đây sau)
}