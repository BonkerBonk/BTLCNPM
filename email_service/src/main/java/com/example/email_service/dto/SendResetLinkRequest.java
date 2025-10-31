package com.example.email_service.dto; // Sửa package nếu cần

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SendResetLinkRequest {
    private String email;
    private String link; // Link reset mật khẩu
}