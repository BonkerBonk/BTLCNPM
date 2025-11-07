package com.btlcnpm.SearchService;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    // (QUAN TRỌNG) Đổi tên này thành tên file .json của bạn
    private static final String FIREBASE_SERVICE_ACCOUNT_KEY = "serviceAccountKey.json";

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                ClassPathResource resource = new ClassPathResource(FIREBASE_SERVICE_ACCOUNT_KEY);
                InputStream serviceAccount = resource.getInputStream();

                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Bean
    public Firestore firestore() {
        // Cung cấp một đối tượng Firestore để các Service khác có thể 'tiêm' (inject) vào
        return FirestoreClient.getFirestore();
    }
}