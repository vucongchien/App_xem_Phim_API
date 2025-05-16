package com.appxemphim.firebaseBackend.dto.response;

public class ShowTimeDTO {
    private String movieId;
    private int seasonNumber;
    private int episodeNumber;
    private String episodeTitle;
    private String releaseTime; // ISO string format: "2025-05-20T20:00:00"
    private int durationInMinutes;

    public ShowTimeDTO() {}

    public ShowTimeDTO(String movieId, int seasonNumber, int episodeNumber,
                       String episodeTitle, String releaseTime, int durationInMinutes) {
        this.movieId = movieId;
        this.seasonNumber = seasonNumber;
        this.episodeNumber = episodeNumber;
        this.episodeTitle = episodeTitle;
        this.releaseTime = releaseTime;
        this.durationInMinutes = durationInMinutes;
    }

    // Getters and Setters
    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
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

    public String getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(String releaseTime) {
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
        return "ShowTimeDTO{" +
                "movieId='" + movieId + '\'' +
                ", seasonNumber=" + seasonNumber +
                ", episodeNumber=" + episodeNumber +
                ", episodeTitle='" + episodeTitle + '\'' +
                ", releaseTime='" + releaseTime + '\'' +
                ", durationInMinutes=" + durationInMinutes +
                '}';
    }
}
