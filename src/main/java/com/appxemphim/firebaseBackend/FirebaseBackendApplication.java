package com.appxemphim.firebaseBackend;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream; // <-- Thêm import này
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets; // <-- Thêm import này

@SpringBootApplication
public class FirebaseBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FirebaseBackendApplication.class, args);
    }

    @PostConstruct
    public void initFirebase() {
        try {
            // Lấy nội dung credentials từ biến môi trường đã tạo trên Render
            String firebaseCredentialsJson = System.getenv("FIREBASE_CREDENTIALS_JSON");

            // Kiểm tra xem biến môi trường có tồn tại không
            if (firebaseCredentialsJson == null || firebaseCredentialsJson.isEmpty()) {
                System.err.println("FATAL ERROR: Environment variable 'FIREBASE_CREDENTIALS_JSON' is not set.");
                // Ném ra lỗi để ứng dụng không thể khởi động nếu thiếu credentials
                throw new IllegalStateException("Environment variable 'FIREBASE_CREDENTIALS_JSON' must be set.");
            }
            
            // Chuyển nội dung String của JSON thành một InputStream
            InputStream serviceAccount = new ByteArrayInputStream(firebaseCredentialsJson.getBytes(StandardCharsets.UTF_8));

            String databaseURL = "https://appxemphim-c633a-default-rtdb.firebaseio.com/";

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(databaseURL)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase initialized successfully from environment variable.");
            } else {
                System.out.println("Firebase already initialized.");
            }

        } catch (IOException e) {
            System.err.println("Failed to initialize Firebase from environment variable.");
            e.printStackTrace();
        }
    }
}