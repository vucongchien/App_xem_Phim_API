package com.appxemphim.firebaseBackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.appxemphim.firebaseBackend.dto.request.ProfileRequest;
import com.appxemphim.firebaseBackend.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {
   private final ProfileService profileService;

    @PatchMapping("/update")
    public ResponseEntity<String> update(@ModelAttribute ProfileRequest profileRequest) {
        return profileService.updateProfile(profileRequest);
    }
}
