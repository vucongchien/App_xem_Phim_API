package com.appxemphim.firebaseBackend.dto.request;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProfileRequest {
    private MultipartFile file;
    private String gmail;
    private String name;   
}
