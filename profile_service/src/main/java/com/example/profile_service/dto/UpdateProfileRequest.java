package com.example.profile_service.dto;

import com.google.cloud.Timestamp;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateProfileRequest {
    
    private String fullName;
    private String phoneNumber;
    private String dateOfBirth;
}
