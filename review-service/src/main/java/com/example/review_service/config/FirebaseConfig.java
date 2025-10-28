package com.example.review_service.config; // (Thay package cho phù hợp)

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource; // <<< IMPORT THÊM
import javax.annotation.PostConstruct;
// import java.io.FileInputStream; // <<< XÓA HOẶC COMMENT DÒNG NÀY
import java.io.IOException;
import java.io.InputStream; // <<< IMPORT THÊM

@Configuration
public class FirebaseConfig {

    // Đặt tên file key của bạn ở đây
    private static final String FIREBASE_SERVICE_ACCOUNT_KEY = "serviceAccountKey.json"; // <<< Đảm bảo tên file này đúng

    @PostConstruct
    public void init() throws IOException {

        InputStream serviceAccountStream = null;
        try {
            // === SỬA ĐOẠN ĐỌC FILE ===
            ClassPathResource resource = new ClassPathResource(FIREBASE_SERVICE_ACCOUNT_KEY);
            serviceAccountStream = resource.getInputStream();

            if (serviceAccountStream == null) {
                throw new IOException("Không tìm thấy file key Firebase trong classpath: " + FIREBASE_SERVICE_ACCOUNT_KEY);
            }
            // === KẾT THÚC SỬA ===

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                // Thêm log để biết đã khởi tạo
                System.out.println("Firebase Application Initialized for " + this.getClass().getPackageName());
            }
        } catch (IOException e) {
            throw new RuntimeException("Lỗi nghiêm trọng khi đọc file key Firebase: " + e.getMessage(), e);
        } finally {
            // Luôn đóng stream
            if (serviceAccountStream != null) {
                serviceAccountStream.close();
            }
        }
    }

    @Bean
    public Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }
}