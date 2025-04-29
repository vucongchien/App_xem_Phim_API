package com.appxemphim.firebaseBackend.Utilities;

import java.io.InputStream;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.RestTemplate;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

@Configuration
public class Firebase {
     @Bean
    public FirebaseDatabase firebaseDatabase() {
        return FirebaseDatabase.getInstance();
    }

    @Bean
    public DatabaseReference databaseReference(FirebaseDatabase firebaseDatabase) {
        return firebaseDatabase.getReference();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    //ggdrive
    @Bean
    public Drive buildDriveService() throws Exception {
        InputStream in = new ClassPathResource("appxemphim-c633a-firebase-adminsdk-fbsvc-7c8017ca3d.json").getInputStream();

        GoogleCredential credential = GoogleCredential.fromStream(in)
                .createScoped(Collections.singleton(DriveScopes.DRIVE_FILE));

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("Firebase Backend")
                .build();
    }
}
