package com.btlcnpm.PushNotificationService.service;

public interface NotificationService {
    void registerDevice(String userId, String fcmToken) throws Exception;
}