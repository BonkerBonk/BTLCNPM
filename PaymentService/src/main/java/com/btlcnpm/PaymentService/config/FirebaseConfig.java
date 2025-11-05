package com.btlcnpm.PaymentService.config; // Đảm bảo đúng package

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.RestTemplate; // Thêm vào

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {


    private static final String FIREBASE_CONFIG_PATH = "firebase-service-account.json";

    @PostConstruct
    public void initializeFirebase() throws IOException {

        if (FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccount = new ClassPathResource(FIREBASE_CONFIG_PATH).getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId("btlcnpm-7e15d") // Lấy từ file JSON
                    .build();

            FirebaseApp.initializeApp(options);
        }
    }


    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore();
    }


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}