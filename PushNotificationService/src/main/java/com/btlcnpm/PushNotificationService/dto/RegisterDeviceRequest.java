package com.btlcnpm.PushNotificationService.dto;

import lombok.Data;

@Data
public class RegisterDeviceRequest {
    private String fcmToken;
}