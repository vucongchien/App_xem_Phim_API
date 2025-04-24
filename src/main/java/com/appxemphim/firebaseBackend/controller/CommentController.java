package com.appxemphim.firebaseBackend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.appxemphim.firebaseBackend.dto.request.CommentRequest;
import com.appxemphim.firebaseBackend.model.Comment;
import com.appxemphim.firebaseBackend.model.Favourite;
import com.appxemphim.firebaseBackend.service.CommentRervice;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentRervice commentRervice;

    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestBody CommentRequest request) {
        try{
            String response = commentRervice.create(request);
            return ResponseEntity.ok(response);
        }catch(IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    
}
