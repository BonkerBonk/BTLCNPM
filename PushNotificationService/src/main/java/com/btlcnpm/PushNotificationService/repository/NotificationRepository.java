package com.btlcnpm.PushNotificationService.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class NotificationRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "device_tokens";


    public void addTokenToUser(String userId, String fcmToken) throws Exception {

        DocumentReference userDocRef = firestore.collection(COLLECTION_NAME).document(userId);

        // Thử cập nhật mảng 'tokens' (document đã tồn tại)
        ApiFuture<WriteResult> future = userDocRef.update("tokens", FieldValue.arrayUnion(fcmToken));

        try {
            future.get(); // Chờ cập nhật xong
        } catch (Exception e) {

            Map<String, Object> newDocData = Map.of(
                    "userId", userId,
                    "tokens", List.of(fcmToken)
            );

            // tạo mới document với ID là userId
            ApiFuture<WriteResult> createFuture = userDocRef.set(newDocData);
            createFuture.get();
        }
    }
}
    
