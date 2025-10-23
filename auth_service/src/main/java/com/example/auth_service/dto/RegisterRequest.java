package com.example.auth_service.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

// @Data bao gồm @Getter, @Setter, @ToString, @EqualsAndHashCode
@Data 
public class RegisterRequest {

    // Tên biến phải khớp với Hợp đồng API [cite: 235]
    private String email;
    private String password;
    private String fullName;
}
