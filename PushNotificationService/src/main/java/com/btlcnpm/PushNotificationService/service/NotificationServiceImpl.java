package com.btlcnpm.PushNotificationService.service;

import com.btlcnpm.PushNotificationService.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public void registerDevice(String userId, String fcmToken) throws Exception {
        if (fcmToken == null || fcmToken.isEmpty()) {
            throw new Exception("fcmToken không được rỗng.");
        }
        if (userId == null || userId.isEmpty()) {
            throw new Exception("userId không được rỗng.");
        }

        notificationRepository.addTokenToUser(userId, fcmToken);
    }
}