package com.appxemphim.firebaseBackend.config; // <-- Có thể đặt trong package config cho gọn

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration // <-- Đánh dấu đây là một lớp cấu hình
public class FirebaseConfig {

    /**
     * Bean này tạo ra credentials từ biến môi trường.
     */
    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        String firebaseCredentialsJson = System.getenv("FIREBASE_CREDENTIALS_JSON");

        if (firebaseCredentialsJson == null || firebaseCredentialsJson.isEmpty()) {
            throw new IllegalStateException("FATAL: Environment variable 'FIREBASE_CREDENTIALS_JSON' is not set.");
        }

        try (InputStream serviceAccount = new ByteArrayInputStream(firebaseCredentialsJson.getBytes(StandardCharsets.UTF_8))) {
            return GoogleCredentials.fromStream(serviceAccount);
        }
    }

    /**
     * Sau khi bean 'googleCredentials' được tạo, phương thức này sẽ được gọi
     * để khởi tạo FirebaseApp.
     */
    @PostConstruct
    public void initializeFirebase(GoogleCredentials credentials) throws IOException {
        // Chúng ta đã có thể quay lại dùng @PostConstruct vì nó không còn nằm trong class chính nữa!
        // Spring sẽ tiêm bean 'googleCredentials' vào đây.
        
        String databaseURL = "https://appxemphim-c633a-default-rtdb.firebaseio.com/";

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setDatabaseUrl(databaseURL)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
            System.out.println("Firebase initialized successfully via configuration class.");
        }
    }
}