package com.appxemphim.firebaseBackend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.appxemphim.firebaseBackend.dto.request.HistoryRequest;
import com.appxemphim.firebaseBackend.exception.ResourceNotFoundException;
import com.appxemphim.firebaseBackend.model.Favourite;
import com.appxemphim.firebaseBackend.model.History;
import com.appxemphim.firebaseBackend.service.HistoryService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryControler {
    private final HistoryService historyService;
    
    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestBody HistoryRequest request) {

        try{
            String response = historyService.create(request);
            return ResponseEntity.ok(response);
        }catch(IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }


    @GetMapping("/getAll")
    public ResponseEntity<?> getMethodName() {
        try{
            List<History> response = historyService.findAllForUID();
            return ResponseEntity.ok(response);
        }catch(IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
    @GetMapping("/by-video/{video_id}")
    public ResponseEntity<?> getHistoryByVideoId(@PathVariable String video_id) {
        try {
            List<History> response = historyService.findByVideoId(video_id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

}
