package com.appxemphim.firebaseBackend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestParam String token,@RequestParam String movieTitle) throws Exception {
        Message message=Message.builder().setToken(token).setNotification(Notification.builder().setTitle("phim yeu thich cap nhat").setBody(movieTitle+" vua co tin moi").build()).build();
        String response=FirebaseMessaging.getInstance().send(message);
        return ResponseEntity.ok("Successfully sent message: " + response);
    }

    
    
}
