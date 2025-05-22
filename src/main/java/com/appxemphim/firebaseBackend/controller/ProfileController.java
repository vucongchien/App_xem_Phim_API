package com.appxemphim.firebaseBackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.appxemphim.firebaseBackend.dto.request.ProfileRequest;
import com.appxemphim.firebaseBackend.dto.response.PersonReviewDTO;
import com.appxemphim.firebaseBackend.service.AccountService;
import com.appxemphim.firebaseBackend.service.ProfileService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {
   private final ProfileService profileService;
   private final AccountService accountService;

    @PatchMapping("/update")
    public ResponseEntity<String> update(@ModelAttribute ProfileRequest profileRequest) {
        return profileService.updateProfile(profileRequest);
    }

    @GetMapping("/information")
    public ResponseEntity<?> getInformation() {
        try{
            PersonReviewDTO reviewDTO = accountService.getInformation("1");
            return  ResponseEntity.ok().body(reviewDTO) ;
        }catch( Exception e){
            e.printStackTrace();
            return  ResponseEntity.status(405).body(e.getMessage());
        }
    }
    
}
