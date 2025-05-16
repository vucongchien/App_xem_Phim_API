package com.appxemphim.firebaseBackend.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.appxemphim.firebaseBackend.Utilities.GoogleUtilities;
import com.appxemphim.firebaseBackend.dto.request.ShowTimeRequest;
import com.appxemphim.firebaseBackend.dto.response.ShowTimeDTO;
import com.appxemphim.firebaseBackend.model.EpisodeInfo;
import com.appxemphim.firebaseBackend.model.ShowTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firestore.v1.Document;
import com.appxemphim.firebaseBackend.dto.request.EpisodeInfoDTO;
import java.time.ZoneOffset;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShowTimesService {
    private static final Logger logger = LoggerFactory.getLogger(ShowTimesService.class);
    private final GoogleUtilities googleUtilities;
    private final Firestore db = FirestoreClient.getFirestore();
    private final ObjectMapper mapper = new ObjectMapper();

    @CacheEvict(value = "showtimes", allEntries = true)
    public String createShowTime(ShowTimeRequest req) {
        if (!StringUtils.hasText(req.getMovieId())) {
            throw new IllegalArgumentException("Movie ID is required");
        }
        List<EpisodeInfo> modelEpisodes = new ArrayList<>();
        for (EpisodeInfoDTO dto : req.getShowTimes()) {
            EpisodeInfo m = new EpisodeInfo();
            m.setSeasonNumber(dto.getSeasonNumber());
            m.setEpisodeNumber(dto.getEpisodeNumber());
            m.setEpisodeTitle(dto.getEpisodeTitle());
            m.setDurationInMinutes(dto.getDurationInMinutes());
            if (dto.getReleaseTime() != null) {
                long epochSec = dto.getReleaseTime().toEpochSecond(ZoneOffset.UTC);
                m.setReleaseTime(Timestamp.ofTimeSecondsAndNanos(epochSec, dto.getReleaseTime().getNano()));
            }
            modelEpisodes.add(m);
        }

        ShowTime entity = new ShowTime();
        entity.setMovieId(req.getMovieId());
        entity.setEpisodes(modelEpisodes);

        db.collection("showtimes")
                .document(req.getMovieId())
                .set(entity);

        return "Showtime for " + req.getMovieId() + " created";
    }

    public Page<ShowTimeDTO> getShowTimesByDay(int day, Pageable pageable) {
        List<ShowTimeDTO> allMatchingEpisodes = new ArrayList<>();
        try {
            var allDocs = db.collection("showtimes").get().get().getDocuments();

            for (var doc : allDocs) {
                ShowTime showTime = doc.toObject(ShowTime.class);
                if (showTime == null || showTime.getEpisodes() == null)
                    continue;

                for (var episode : showTime.getEpisodes()) {
                    Timestamp release = episode.getReleaseTime();
                    if (release == null)
                        continue;

                    Instant instant = Instant.ofEpochSecond(release.getSeconds());
                    ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
                    int dayOfWeek = zonedDateTime.getDayOfWeek().getValue() % 7; // 0 = Sunday

                    if (dayOfWeek == day) {
                        ShowTimeDTO dto = new ShowTimeDTO();
                        dto.setMovieId(showTime.getMovieId());
                        dto.setSeasonNumber(episode.getSeasonNumber());
                        dto.setEpisodeNumber(episode.getEpisodeNumber());
                        dto.setEpisodeTitle(episode.getEpisodeTitle());
                        dto.setDurationInMinutes(episode.getDurationInMinutes());
                        dto.setReleaseTime(release.toString());
                        allMatchingEpisodes.add(dto);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching showtimes by day {}: {}", day, e.getMessage(), e);
            return Page.empty(pageable); // ✅ Không ném lỗi lên nữa
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allMatchingEpisodes.size());

        if (start >= allMatchingEpisodes.size()) {
            return Page.empty(pageable); // ✅ Tránh IndexOutOfBounds
        }

        List<ShowTimeDTO> pagedList = allMatchingEpisodes.subList(start, end);
        return new PageImpl<>(pagedList, pageable, allMatchingEpisodes.size());
    }

    public Page<ShowTimeDTO> getShowTimesInWeek(Pageable pageable) {
        try {
            List<ShowTimeDTO> allInWeek = new ArrayList<>();

            Timestamp now = Timestamp.now();
            Timestamp endOfWeek = Timestamp.ofTimeSecondsAndNanos(
                    now.getSeconds() + 7 * 24 * 60 * 60, // +7 days
                    0);

            var allDocs = db.collection("showtimes").get().get().getDocuments();

            for (var doc : allDocs) {
                ShowTime showTime = doc.toObject(ShowTime.class);
                if (showTime == null || showTime.getEpisodes() == null)
                    continue;

                for (var episode : showTime.getEpisodes()) {
                    Timestamp release = episode.getReleaseTime();
                    if (release != null &&
                            release.compareTo(now) >= 0 &&
                            release.compareTo(endOfWeek) <= 0) {

                        ShowTimeDTO dto = new ShowTimeDTO();
                        dto.setMovieId(showTime.getMovieId());
                        dto.setSeasonNumber(episode.getSeasonNumber());
                        dto.setEpisodeNumber(episode.getEpisodeNumber());
                        dto.setEpisodeTitle(episode.getEpisodeTitle());
                        dto.setDurationInMinutes(episode.getDurationInMinutes());
                        dto.setReleaseTime(release.toString());

                        allInWeek.add(dto);
                    }
                }

            }
            // manual pagination
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), allInWeek.size());
            List<ShowTimeDTO> pageList = allInWeek.subList(start, end);

            return new PageImpl<>(pageList, pageable, allInWeek.size());
        } catch (Exception e) {
            logger.error("Error fetching weekly showtimes: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch weekly showtimes", e);
        }
    }

    /**
     * Cập nhật toàn bộ danh sách episodes cho movieId.
     */
    @CacheEvict(value = "showtimes", allEntries = true)
    public String updateShowTime(ShowTimeRequest showTimeRequest) {
        if (!StringUtils.hasText(showTimeRequest.getMovieId())) {
            throw new IllegalArgumentException("Movie ID is required");
        }
        List<EpisodeInfo> modelEpisodes = new ArrayList<>();
        for (EpisodeInfoDTO dto : showTimeRequest.getShowTimes()) {
            EpisodeInfo m = new EpisodeInfo();
            m.setSeasonNumber(dto.getSeasonNumber());
            m.setEpisodeNumber(dto.getEpisodeNumber());
            m.setEpisodeTitle(dto.getEpisodeTitle());
            m.setDurationInMinutes(dto.getDurationInMinutes());
            if (dto.getReleaseTime() != null) {
                long epochSec = dto.getReleaseTime().toEpochSecond(ZoneOffset.UTC);
                m.setReleaseTime(Timestamp.ofTimeSecondsAndNanos(epochSec, dto.getReleaseTime().getNano()));
            }
            modelEpisodes.add(m);
        }
        if (modelEpisodes == null) {
            throw new IllegalArgumentException("List of episodes is required");
        }
        DocumentReference docRef = db.collection("showtimes").document(showTimeRequest.getMovieId());
        // Cập nhật field "episodes" (overwrite)
        try {
            docRef.update("episodes", modelEpisodes).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            logger.error("Error updating showtime: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update showtime", e);
        }

        logger.info("Updated showtime for movieId = {}", showTimeRequest.getMovieId());
        return "Showtime for movieId = " + showTimeRequest.getMovieId() + " updated successfully";

    }

    /**
     * Xóa toàn bộ document showtime theo movieId.
     */
    @CacheEvict(value = "showtimes", allEntries = true)
    public String deleteShowTime(String movieId) {
        try {
            if (!StringUtils.hasText(movieId)) {
                throw new IllegalArgumentException("Movie ID is required");
            }

            DocumentReference docRef = db.collection("showtimes").document(movieId);
            docRef.delete().get();

            logger.info("Deleted showtime for movieId = {}", movieId);
            return "Showtime for movieId = " + movieId + " deleted successfully";
        } catch (Exception e) {
            logger.error("Error deleting showtime: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete showtime", e);
        }
    }

}
