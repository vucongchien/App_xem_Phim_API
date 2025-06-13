package com.appxemphim.firebaseBackend.Utilities;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory; // Sử dụng GsonFactory cho nhất quán
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.firebase.FirebaseApp; // <-- Thêm import này
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.io.IOException; // Thêm import cho Exception
import java.security.GeneralSecurityException; // Thêm import cho Exception
import java.util.Collections;

@Configuration
public class Firebase {
    @Bean
    public FirebaseDatabase firebaseDatabase() {
        // Trả về instance đã được khởi tạo ở class chính
        return FirebaseDatabase.getInstance(FirebaseApp.getInstance());
    }

    @Bean
    public DatabaseReference databaseReference(FirebaseDatabase firebaseDatabase) {
        return firebaseDatabase.getReference();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // --- GOOGLE DRIVE ---
    // Phương thức này đã được sửa lại hoàn toàn để không đọc file nữa
    @Bean
    public Drive buildDriveService() throws GeneralSecurityException, IOException {
        // BƯỚC 1: Lấy credentials đã được khởi tạo trong FirebaseApp (từ biến môi trường)
        var credentials = FirebaseApp.getInstance().getOptions().getCredentials();

        // BƯỚC 2: Sử dụng credentials đó để tạo Drive service
        // HttpCredentialsAdapter là "cầu nối" để chuyển đổi credentials của Firebase cho Drive service
        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(), // Sử dụng GsonFactory
                new HttpCredentialsAdapter(credentials.createScoped(Collections.singleton(DriveScopes.DRIVE)))
        ).setApplicationName("Firebase Backend").build();
    }
}