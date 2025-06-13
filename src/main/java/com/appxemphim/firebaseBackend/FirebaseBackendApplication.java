package com.appxemphim.firebaseBackend;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
     * Bean này vẫn được giữ nguyên. Nó tạo ra credentials từ biến môi trường.
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
     * SỬ DỤNG HÀM KHỞI TẠO ĐỂ INIT FIREBASE
     * Spring sẽ tự động "tiêm" bean 'googleCredentials' vào đây khi khởi tạo ứng dụng.
     * Toàn bộ logic trong @PostConstruct cũ được chuyển vào đây.
     */
    public FirebaseBackendApplication(GoogleCredentials credentials) {
        try {
            String databaseURL = "https://appxemphim-c633a-default-rtdb.firebaseio.com/";

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setDatabaseUrl(databaseURL)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase initialized successfully via constructor.");
            } else {
                System.out.println("Firebase already initialized.");
            }

        } catch (Exception e) {
            System.err.println("Failed to initialize Firebase.");
            // Ném lỗi ra ngoài để ứng dụng dừng lại nếu không thể khởi tạo Firebase
            throw new RuntimeException("Could not initialize Firebase.", e);
        }
    }
}