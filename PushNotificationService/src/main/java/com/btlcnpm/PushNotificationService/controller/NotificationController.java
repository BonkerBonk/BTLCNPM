package com.btlcnpm.PushNotificationService.controller;

import com.btlcnpm.PushNotificationService.dto.RegisterDeviceRequest;
import com.btlcnpm.PushNotificationService.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/push") // Dùng /push theo bảng quy ước đã sửa
public class NotificationController {

    @Autowired
    private NotificationService notificationService;


     //Đăng ký thiết bị

    @PostMapping("/register-device")
    public ResponseEntity<?> registerDevice(@RequestBody RegisterDeviceRequest request) {

        // TODO: Lấy userId từ Token
        String userId = "temp_user_id_123";

        try {
            notificationService.registerDevice(userId, request.getFcmToken());


            return ResponseEntity.ok(Map.of("message", "Đăng ký thiết bị thành công."));

        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }
}