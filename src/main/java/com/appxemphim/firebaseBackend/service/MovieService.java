package com.appxemphim.firebaseBackend.service;

import com.appxemphim.firebaseBackend.Utilities.GoogleUtilities;
import com.appxemphim.firebaseBackend.dto.request.MovieRequest;
import com.appxemphim.firebaseBackend.dto.response.MovieDTO;
import com.appxemphim.firebaseBackend.exception.ResourceNotFoundException;
import com.appxemphim.firebaseBackend.model.Movie;
import com.appxemphim.firebaseBackend.model.Review;
import com.appxemphim.firebaseBackend.repository.MovieRepository;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private static final Logger logger = LoggerFactory.getLogger(MovieService.class);
    private final MovieRepository movieRepository;
    private final GoogleUtilities googleUtilities;
    private final VideoService videoService;
    private final PersonService personService;
    private final GenresService genresService;
    private final Firestore db = FirestoreClient.getFirestore();

   
    public String create(MovieRequest movieRequest) {
        try {
            Movie movie = new Movie();
            DocumentReference docRef = db.collection("Movies").document();

            movie.setMovie_Id(docRef.getId());
            movie.setTitle(movieRequest.getTitle());
            movie.setDescription(movieRequest.getDescription());
            movie.setPoster_url(googleUtilities.exportLink(movieRequest.getPoster_url()));
            movie.setTrailer_url(googleUtilities.exportLink(movieRequest.getTrailer_url()));
            movie.setRating(movieRequest.getRating());
            movie.setNation(movieRequest.getNation());
            movie.setCreated_at(movieRequest.getCreated_at());

            docRef.set(movie).get();
            logger.info("Created movie with ID: {}", movie.getMovie_Id());
            return "Thêm phim thành công!";
        } catch (Exception e) {
            logger.error("Failed to create movie: {}", e.getMessage(), e);
            throw new RuntimeException("Thêm phim thất bại: " + e.getMessage());
        }
    }

    @Cacheable(value = "movies", key = "#id")
    public MovieDTO getMovieById(String id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with ID: " + id));
        return enrichMovieDTO(movie, Set.of("genres", "actors", "directors", "videos", "reviews"));
    }


    public Page<MovieDTO> searchMovies(
            String title,
            List<String> genres,
            List<Integer> years,
            List<String> nations,
            double minRating,
            Pageable pageable,
            Set<String> include) {
        List<Movie> movies = movieRepository.searchMovies(title, genres, years, nations, minRating);
        List<MovieDTO> movieDTOs = movies.stream()
                .map(movie -> enrichMovieDTO(movie, include != null ? include : Set.of("genres")))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), movieDTOs.size());
        List<MovieDTO> pagedMovies = movieDTOs.subList(start, end);

        return new PageImpl<>(pagedMovies, pageable, movieDTOs.size());
    }

    // đóng gói thông tin của movie thành movieDTO để trả về cho client
    private MovieDTO enrichMovieDTO(Movie movie, Set<String> include) {
        MovieDTO dto = new MovieDTO();
        dto.setMovie_Id(movie.getMovie_Id());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setPoster_url(movie.getPoster_url());
        dto.setTrailer_url(movie.getTrailer_url());
        dto.setRating(movie.getRating());
        dto.setNation(movie.getNation());
        dto.setCreated_at(movie.getCreated_at());

        String movieId = movie.getMovie_Id();
        try {
            if (include.contains("genres")) {
                dto.setGenres(genresService.getAllForMovie(movieId));
            }
            if (include.contains("actors")) {
                dto.setActors(personService.findALLActorForMovie(movieId, "Movie_Actor"));
            }
            if (include.contains("directors")) {
                dto.setDirectors(personService.findALLActorForMovie(movieId, "Movie_Directors"));
            }
            if (include.contains("videos")) {
                dto.setVideos(videoService.getAllForMideo(movieId));
            }
            if (include.contains("reviews")) {
                dto.setReviews(getReviewsForMovie(movieId));
            }
        } catch (Exception e) {
            logger.error("Failed to enrich movie DTO for ID {}: {}", movieId, e.getMessage());
        }

        return dto;
    }

    private List<Review> getReviewsForMovie(String movieId) {
        try {
            DocumentReference docRef = db.collection("Movies").document(movieId);
            DocumentSnapshot snapshot = docRef.get().get();
            return (List<Review>) snapshot.get("reviews");
        } catch (Exception e) {
            logger.error("Failed to fetch reviews for movie ID {}: {}", movieId, e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<?> getMovieDetails(String movieId, String detailType) {
        switch (detailType.toLowerCase()) {
            case "videos":
                return videoService.getAllForMideo(movieId);
            case "actors":
                return personService.findALLActorForMovie(movieId, "Movie_Actor");
            case "directors":
                return personService.findALLActorForMovie(movieId, "Movie_Directors");
            case "reviews":
                return getReviewsForMovie(movieId);
            case "genres":
                return genresService.getAllForMovie(movieId);
            default:
                throw new IllegalArgumentException("Invalid detail type: " + detailType);
        }
    }
}