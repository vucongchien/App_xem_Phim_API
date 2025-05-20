package com.appxemphim.firebaseBackend.model;


import com.google.cloud.Timestamp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EpisodeInfo {
    private int seasonNumber;
    private int episodeNumber;
    private String episodeTitle;
    private Timestamp releaseTime;
    private int durationInMinutes;

    public EpisodeInfo(int seasonNumber, int episodeNumber, String episodeTitle,
            Timestamp releaseTime, int durationInMinutes) {
        this.seasonNumber = seasonNumber;
        this.episodeNumber = episodeNumber;
        this.episodeTitle = episodeTitle;
        this.releaseTime = releaseTime;
        this.durationInMinutes = durationInMinutes;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(int seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(int episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public String getEpisodeTitle() {
        return episodeTitle;
    }

    public void setEpisodeTitle(String episodeTitle) {
        this.episodeTitle = episodeTitle;
    }

    public Timestamp getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(Timestamp releaseTime) {
        this.releaseTime = releaseTime;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public void setDurationInMinutes(int durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }

    @Override
    public String toString() {
        return "EpisodeInfo{" +
                "seasonNumber=" + seasonNumber +
                ", episodeNumber=" + episodeNumber +
                ", episodeTitle='" + episodeTitle + '\'' +
                ", releaseTime=" + releaseTime +
                ", durationInMinutes=" + durationInMinutes +
                '}';
    }
}
