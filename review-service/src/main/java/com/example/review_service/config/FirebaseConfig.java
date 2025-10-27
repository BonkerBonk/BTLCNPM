package com.example.review_service.config; // Đảm bảo package đúng

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;        // Import Firestore
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;   // Import FirestoreClient
import org.springframework.context.annotation.Bean;     // Import @Bean
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource; // <<< Import ClassPathResource

import javax.annotation.PostConstruct; // Hoặc jakarta.annotation.PostConstruct
import java.io.IOException;
import java.io.InputStream;            // <<< Import InputStream

@Configuration
public class FirebaseConfig {

    // Tên file key JSON (đảm bảo file này tồn tại trong src/main/resources)
    private static final String FIREBASE_SERVICE_ACCOUNT_KEY = "serviceAccountKey.json";

    @PostConstruct // Hàm này sẽ chạy khi bean được tạo
    public void init() throws IOException {
        // --- Sửa đoạn đọc file ---
        InputStream serviceAccountStream = null; // Khởi tạo là null
        try {
            // Sử dụng ClassPathResource để lấy file từ classpath (resources)
            ClassPathResource resource = new ClassPathResource(FIREBASE_SERVICE_ACCOUNT_KEY);
            serviceAccountStream = resource.getInputStream(); // Lấy InputStream

            // Kiểm tra xem file có thực sự đọc được không
            if (serviceAccountStream == null) {
                throw new IOException("Không thể tìm thấy file key Firebase trong classpath: " + FIREBASE_SERVICE_ACCOUNT_KEY);
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                    // Dùng InputStream đã lấy được
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                    .build();

            // Chỉ khởi tạo FirebaseApp nếu chưa có app nào được khởi tạo
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Application Initialized for review-service"); // Log thông báo
            }
        } catch (IOException e) {
            System.err.println("!!! LỖI NGHIÊM TRỌNG KHI ĐỌC FILE KEY FIREBASE: " + e.getMessage());
            throw e; // Ném lại lỗi để Spring biết khởi tạo thất bại
        } finally {
            // Đóng InputStream sau khi dùng xong (rất quan trọng)
            if (serviceAccountStream != null) {
                try {
                    serviceAccountStream.close();
                } catch (IOException e) {
                    System.err.println("Lỗi khi đóng InputStream file key Firebase: " + e.getMessage());
                }
            }
        }
        // --- Kết thúc sửa đoạn đọc file ---
    }

    // Bean này cung cấp đối tượng Firestore cho các service khác
    @Bean
    public Firestore getFirestore() {
        // Lấy Firestore instance đã được khởi tạo bởi Admin SDK
        return FirestoreClient.getFirestore();
    }
}