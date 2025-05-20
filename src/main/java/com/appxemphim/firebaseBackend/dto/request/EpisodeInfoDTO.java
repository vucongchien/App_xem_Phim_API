package com.appxemphim.firebaseBackend.dto.request;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EpisodeInfoDTO {
    private int seasonNumber;
    private int episodeNumber;
    private String episodeTitle;
    private LocalDateTime releaseTime;  
    private int durationInMinutes;
}