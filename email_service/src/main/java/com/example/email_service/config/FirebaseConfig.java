package com.example.email_service.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
@Configuration
public class FirebaseConfig {

    @PostConstruct // Đảm bảo hàm này chạy sau khi FirebaseConfig được tạo
    public void initialize() {
        try {
            // Đọc file JSON từ thư mục "resources"
            InputStream serviceAccount = getClass().getClassLoader()
                    .getResourceAsStream("btlcnpm-7e15d-firebase-adminsdk-fbsvc-c1929eb959.json");

            if (serviceAccount == null) {
                throw new IOException("Không tìm thấy file serviceAccountKey.json trong thư mục resources");
            }

            // Lấy URL database từ Firebase Console (Cài đặt dự án > Tổng quan)
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://btlcnpm-7e15d.firebaseio.com")
                    .build();

            // Khởi tạo app, chỉ 1 lần duy nhất
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

        } catch (IOException e) {
            e.printStackTrace();
            // Xử lý lỗi nghiêm trọng: không thể khởi tạo Firebase
            // Bạn có thể throw một RuntimeException ở đây để dừng ứng dụng
        }
    }

    // Cung cấp 2 Bean để các Service khác có thể @Autowired
    
    @Bean
    public FirebaseAuth firebaseAuth() {
        // Trả về thể hiện (instance) của FirebaseAuth
        return FirebaseAuth.getInstance();
    }

    @Bean
    public Firestore firestore() {
        // Trả về thể hiện (instance) của Firestore
        return FirestoreClient.getFirestore();
    }
}
