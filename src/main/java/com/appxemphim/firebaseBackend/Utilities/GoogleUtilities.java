package com.appxemphim.firebaseBackend.Utilities;

import java.io.FileOutputStream;
import java.util.Collections;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;


import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive; 
import com.google.api.services.drive.model.File;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GoogleUtilities {
    @Value("${google.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final Drive drive;

    @Async
    public long getVideoDuration(String fileLink) {
        String fileId = extractFileId(fileLink);
        try {
            String apiUrl = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/drive/v3/files/" + fileId)
                    .queryParam("fields", "videoMediaMetadata(durationMillis)")
                    .queryParam("key", apiKey)
                    .toUriString();

            // Gọi API bằng RestTemplate
            String response = restTemplate.getForObject(apiUrl, String.class);
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("videoMediaMetadata")) {
                long durationMillis = jsonObject.getJSONObject("videoMediaMetadata").getLong("durationMillis");
                return durationMillis;
            } else {
                throw new RuntimeException("lỗi lấy thời lượng video");
            }

        }catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    private String extractFileId(String driveUrl) {
        String regex = ".*(?:id=|/d/)([a-zA-Z0-9_-]+).*";
        return driveUrl.matches(regex) ? driveUrl.replaceAll(regex, "$1") : null;
    }

    public String exportLink(String driveUrl) {
        String fileId = extractFileId(driveUrl);
        if(fileId == null){
            throw new RuntimeException("lỗi lấy đường dẫn video");
        }
        return  "https://drive.google.com/uc?export=download&id=" + fileId;
    }


    public String uploadImage(MultipartFile multipartFile) {
        try {
            File fileMetadata = new File();
            fileMetadata.setName(multipartFile.getOriginalFilename());
    
            // Thêm file vào thư mục có folderId
            fileMetadata.setParents(Collections.singletonList("14chIuUrZR3YU4uEqYQte1gUiSaZJaSyv"));
    
            java.io.File tempFile = java.io.File.createTempFile("upload", multipartFile.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(multipartFile.getBytes());
            }
    
            FileContent mediaContent = new FileContent(multipartFile.getContentType(), tempFile);
    
            File uploadedFile = drive.files().create(fileMetadata, mediaContent)
                    .setFields("id, webViewLink")
                    .execute();
    
            return exportLink(uploadedFile.getWebViewLink());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi upload ảnh lên Google Drive: " + e.getMessage(), e);
        }
    }
    

}


