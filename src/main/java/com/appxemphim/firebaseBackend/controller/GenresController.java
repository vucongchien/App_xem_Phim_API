package com.appxemphim.firebaseBackend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;

import com.appxemphim.firebaseBackend.model.Genres;
import com.appxemphim.firebaseBackend.service.GenresService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenresController {
    private final GenresService genresService;

    @GetMapping("/{movie_id}")
    public List<Genres> findAllForMovie(@PathVariable String movie_id) {
        return genresService.getAllForMovie(movie_id);
    }
    
    @GetMapping("/getAll")
    public ResponseEntity<?> getMethodName() {
       try{
        List<Genres> genres = genresService.getAllFGenres();
        if (genres != null) {
            return ResponseEntity.ok(genres);
        } else {
            return ResponseEntity.notFound().build();
        }
       }catch(RuntimeException e) {
        return ResponseEntity.status(401).body(e.getMessage());
    }
    }
    
}
