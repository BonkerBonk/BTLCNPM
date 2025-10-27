package com.example.Movie_Catalog_Service.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct; // Hoặc javax.annotation.PostConstruct
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource; // <<< Import thư viện này

import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    // Tên file JSON key của bạn
    private static final String FIREBASE_SERVICE_ACCOUNT_KEY = "serviceAccountKey.json";

    @PostConstruct // Đảm bảo annotation này tồn tại
    public void init() { // Tên hàm có thể khác (vd: initialize)
        try {
            // <<< SỬA ĐOẠN NÀY >>>
            // Sử dụng ClassPathResource để đọc file từ thư mục resources
            ClassPathResource resource = new ClassPathResource(FIREBASE_SERVICE_ACCOUNT_KEY);
            InputStream serviceAccountStream = resource.getInputStream(); // Lấy InputStream

            // Kiểm tra xem InputStream có null không (đề phòng)
            if (serviceAccountStream == null) {
                throw new RuntimeException("Không thể tìm thấy file key Firebase trong classpath: " + FIREBASE_SERVICE_ACCOUNT_KEY);
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountStream)) // Dùng InputStream ở đây
                    // .setDatabaseUrl("...") // Thêm nếu cần
                    .build();

            // Khởi tạo FirebaseApp nếu chưa có
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Application Initialized for Movie-Catalog-Service"); // Thêm log
            }

        } catch (Exception e) {
            // Ném lỗi rõ ràng hơn nếu có vấn đề
            throw new RuntimeException("Lỗi khởi tạo Firebase Admin SDK", e);
        }
    }

    // (Thêm @Bean cho Firestore nếu cần)
    // @Bean
    // public Firestore firestore() { ... }
}