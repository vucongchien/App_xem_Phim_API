package com.appxemphim.firebaseBackend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appxemphim.firebaseBackend.Utilities.GoogleUtilities;
import com.appxemphim.firebaseBackend.model.Movie;
import com.appxemphim.firebaseBackend.model.Video;
import com.appxemphim.firebaseBackend.service.VideoService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
public class VideoController {
    private final VideoService videoService;
    
   @GetMapping("/{movie_id}")
   public ResponseEntity<List<Video>> getAllForMovie(@PathVariable String movie_id) {
       try{
            List<Video> videos = videoService.getAllVideosForMovie(movie_id);
            return ResponseEntity.ok(videos);
       }catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
       }
   }
   
    
}
