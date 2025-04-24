package com.appxemphim.firebaseBackend.dto.request;

import lombok.Data;

import javax.validation.constraints.*;

import com.google.cloud.Timestamp;

import java.util.Date;

@Data
public class MovieRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotBlank(message = "Poster URL is required")
    private String poster_url;

    @NotBlank(message = "Trailer URL is required")
    private String trailer_url;

    @NotBlank(message = "Nation is required")
    private String nation;

    @NotNull(message = "Created date is required")
    private Date created_at;
}