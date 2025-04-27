package com.appxemphim.firebaseBackend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.appxemphim.firebaseBackend.dto.request.ReviewRequest;
import com.appxemphim.firebaseBackend.model.Review;
import com.appxemphim.firebaseBackend.service.ReviewService;

import lombok.RequiredArgsConstructor;
import okhttp3.ResponseBody;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody; 

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("/create")
    public String create(@RequestBody ReviewRequest request) {
        return reviewService.create(request);
    }
    
    @GetMapping("/get/{movie_id}")
    public ResponseEntity<?> getMethodName(@PathVariable String movie_id) {
        try{
            List<Review> response = reviewService.getallReviewForMovie(movie_id);
            return ResponseEntity.ok().body(response);

        }catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Không tìm thấy đánh giá: " + e.getMessage());
        }
    }
    
}
