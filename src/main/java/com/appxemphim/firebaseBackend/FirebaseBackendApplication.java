package com.appxemphim.firebaseBackend;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean; // <-- Thêm import này
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class FirebaseBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FirebaseBackendApplication.class, args);
    }

    /**
     * Tạo một Bean để quản lý GoogleCredentials.
     * Bean này sẽ được Spring nạp vào những nơi cần dùng (Dependency Injection).
     */
    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        String firebaseCredentialsJson = System.getenv("FIREBASE_CREDENTIALS_JSON");

        if (firebaseCredentialsJson == null || firebaseCredentialsJson.isEmpty()) {
            throw new IllegalStateException("Environment variable 'FIREBASE_CREDENTIALS_JSON' must be set.");
        }
        
        try (InputStream serviceAccount = new ByteArrayInputStream(firebaseCredentialsJson.getBytes(StandardCharsets.UTF_8))) {
            return GoogleCredentials.fromStream(serviceAccount);
        }
    }

    /**
     * Sử dụng @PostConstruct để đảm bảo Firebase được khởi tạo sau khi tất cả các Bean đã sẵn sàng.
     * Chúng ta sẽ "tiêm" GoogleCredentials Bean đã tạo ở trên vào đây.
     */
    @PostConstruct
    public void initFirebase(GoogleCredentials credentials) { // <-- "Tiêm" credentials vào đây
        try {
            String databaseURL = "https://appxemphim-c633a-default-rtdb.firebaseio.com/";

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials) // <-- Sử dụng credentials đã được tiêm vào
                    .setDatabaseUrl(databaseURL)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase initialized successfully.");
            } else {
                System.out.println("Firebase already initialized.");
            }

        } catch (Exception e) {
            System.err.println("Failed to initialize Firebase.");
            e.printStackTrace();
        }
    }
}