package com.appxemphim.firebaseBackend.controller;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.checkerframework.framework.qual.PolymorphicQualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appxemphim.firebaseBackend.dto.request.ShowTimeRequest;
import com.appxemphim.firebaseBackend.dto.response.ShowTimeDTO;
import com.appxemphim.firebaseBackend.service.ShowTimesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/showtimes")
@RequiredArgsConstructor
@Validated
public class ShowTimesController {
    private static final Logger logger = LoggerFactory.getLogger(ShowTimesController.class);
    private final ShowTimesService showTimesService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Firestore db = FirestoreClient.getFirestore();

    @PostMapping
    public ResponseEntity<String> postShowTime(@Valid @RequestBody ShowTimeRequest showTimeRequest) {
        logger.info("Received request to create showtime: {}",
                showTimeRequest.getMovieId() + " " + showTimeRequest.getShowTimes());

        String response = showTimesService.createShowTime(showTimeRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ShowTimeDTO>> getShowTimes(
            @RequestParam(required = false, defaultValue = "-1") @Min(0) @Max(6) int day,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        logger.info("Fetching movies with filters:  day={}", day);
        Pageable pageable = PageRequest.of(page, size);
        if (day >= 0 && day <= 6) {
            Page<ShowTimeDTO> showTimes = showTimesService.getShowTimesByDay(day, pageable);
            return ResponseEntity.ok(showTimes);
        }

        Page<ShowTimeDTO> response = showTimesService.getShowTimesInWeek(pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateShowTime(@Valid @RequestBody ShowTimeRequest showTimeRequest) {
        logger.info("Received request to update showtime: {}",
                showTimeRequest.getMovieId() + " " + showTimeRequest.getShowTimes());
        // Validate the request
        try {
            showTimesService.updateShowTime(showTimeRequest);
            return ResponseEntity.ok("Showtime updated successfully");
        } catch (Exception e) {
            logger.error("Error updating showtime: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating showtime");
        }
    }

    @DeleteMapping("/delete/{movieId}")
    public ResponseEntity<?> deleteShowTime(@PathVariable String movieId) {
        logger.info("Received request to delete showtime: {}", movieId);
        // Validate the request
        try {
            showTimesService.deleteShowTime(movieId);
            return ResponseEntity.ok("Showtime deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting showtime: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting showtime");
        }
    }
}
