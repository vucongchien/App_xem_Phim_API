package com.appxemphim.firebaseBackend.Utilities;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials; // <-- Thêm import
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
public class Firebase {
    @Bean
    public FirebaseDatabase firebaseDatabase() {
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
    // Phương thức này giờ sẽ nhận GoogleCredentials làm tham số
    @Bean
    public Drive buildDriveService(GoogleCredentials credentials) throws GeneralSecurityException, IOException { // <-- "Tiêm" credentials vào đây
        
        // Bây giờ chúng ta không cần lấy credentials từ FirebaseApp nữa,
        // mà sử dụng trực tiếp cái đã được tiêm vào.
        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials.createScoped(Collections.singleton(DriveScopes.DRIVE)))
        ).setApplicationName("Firebase Backend").build();
    }
}