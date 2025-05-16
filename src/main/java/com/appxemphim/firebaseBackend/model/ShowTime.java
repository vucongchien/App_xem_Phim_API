package com.appxemphim.firebaseBackend.model;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShowTime {
    private String movieId;
    private List<EpisodeInfo> episodes; 

    public ShowTime(String movieId, List<EpisodeInfo> episodes) {
        this.movieId = movieId;
        this.episodes = episodes;
    }

    @Override
    public String toString() {
        return "ShowTime{" +
                "movieId='" + movieId + '\'' +
                ", episodes=" + episodes +
                '}';
    }
}
