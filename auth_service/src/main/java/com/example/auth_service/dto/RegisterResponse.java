package com.example.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor // Thêm constructor có đủ tham số
public class RegisterResponse {
    private String userId;
    private String email;
    private String fullName;
}