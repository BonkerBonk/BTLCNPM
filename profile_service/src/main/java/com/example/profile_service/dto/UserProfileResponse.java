package com.example.profile_service.dto;

import com.google.cloud.Timestamp; // Import Timestamp
import lombok.Builder;
import lombok.Data;

@Data
@Builder // Dùng @Builder để dễ tạo đối tượng
public class UserProfileResponse {

    private String userId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String profileImageUrl;
    private String dateOfBirth; // Giữ kiểu Timestamp
}