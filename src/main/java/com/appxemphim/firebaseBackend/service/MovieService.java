package com.appxemphim.firebaseBackend.service;

import com.appxemphim.firebaseBackend.Utilities.GoogleUtilities;
import com.appxemphim.firebaseBackend.dto.request.MovieRequest;
import com.appxemphim.firebaseBackend.dto.response.MovieDTO;
import com.appxemphim.firebaseBackend.exception.ResourceNotFoundException;
import com.appxemphim.firebaseBackend.model.Movie;
import com.appxemphim.firebaseBackend.model.Review;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.model.SearchResult;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import com.google.cloud.Timestamp;
import java.time.ZoneId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MovieService {

    private static final Logger logger = LoggerFactory.getLogger(MovieService.class);
    private final GoogleUtilities googleUtilities;
    private final VideoService videoService;
    private final PersonService personService;
    private final GenresService genresService;
    private final Firestore db = FirestoreClient.getFirestore();
    private final ObjectMapper mapper = new ObjectMapper();

    private final Client meiliClient;

    /**
     * Khởi tạo index “movies” trên MeiliSearch:
     * - Tạo nếu chưa có, đặt primaryKey
     * - Cấu hình searchable/filterable/sortable
     */
    @PostConstruct
    public void initMeiliIndex() throws Exception {
        // 1) Tạo index với primaryKey = "movie_Id" (nếu đã có thì sẽ ném lỗi, catch bỏ qua)
        try {
            meiliClient.createIndex("movies", "movie_Id");
        } catch (Exception ignored) {
        }

        // 2) Lấy index
        Index index = meiliClient.index("movies");

        // 3) Cấu hình các trường để search / filter / sort
        index.updateSearchableAttributesSettings(new String[] {
                "title",
                "description"
        });
        index.updateFilterableAttributesSettings(new String[] {
                "genres",
                "nation",
                "rating",
                "years"
        });
        reindexAll();
    }

    public void reindexAll() throws Exception {
        List<Movie> all = db.collection("Movies").get().get().getDocuments().stream()
                .map(d -> {
                    Movie m = d.toObject(Movie.class);
                    m.setMovie_Id(d.getId());
                    return m;
                })
                .collect(Collectors.toList());
        String jsonArray = mapper.writeValueAsString(all);
        meiliClient.index("movies").addDocuments(jsonArray);
    }

    @CacheEvict(value = "movies", allEntries = true)
    public String create(MovieRequest movieRequest) {
        try {
            // Validate input
            if (!StringUtils.hasText(movieRequest.getTitle())) {
                throw new IllegalArgumentException("Title cannot be empty");
            }
            if (!StringUtils.hasText(movieRequest.getPoster_url())) {
                throw new IllegalArgumentException("Poster URL cannot be empty");
            }

            Movie movie = new Movie();
            DocumentReference docRef = db.collection("Movies").document();

            movie.setMovie_Id(docRef.getId());
            movie.setTitle(movieRequest.getTitle());
            movie.setDescription(movieRequest.getDescription());
            movie.setPoster_url(googleUtilities.exportLink(movieRequest.getPoster_url()));
            movie.setTrailer_url(googleUtilities.exportLink(movieRequest.getTrailer_url()));
            movie.setNation(movieRequest.getNation());
            movie.setCreated_at(Timestamp.of(movieRequest.getCreated_at()));

            docRef.set(movie).get();

            // Cập nhật MeiliSearch ngay lập tức
            try {
                String jsonMovie = mapper.writeValueAsString(movie);
                meiliClient.index("movies").addDocuments(jsonMovie);
            } catch (Exception meiliEx) {
                logger.error("Failed to update MeiliSearch for movie ID: {}", movie.getMovie_Id(), meiliEx);
                // Có thể thêm logic thử lại hoặc rollback Firestore nếu cần
            }

            logger.info("Created movie with ID: {}", movie.getMovie_Id());
            return "Thêm phim thành công!";
        } catch (Exception e) {
            logger.error("Failed to create movie: {}", e.getMessage(), e);
            throw new RuntimeException("Thêm phim thất bại: " + e.getMessage());
        }
    }

    public MovieDTO getMovieDTOById(String id) throws Exception {
        try {
            DocumentReference docRef = db.collection("Movies").document(id.trim());
            DocumentSnapshot snapshot = docRef.get().get();

            if (!snapshot.exists()) {
                throw new ResourceNotFoundException("Movie not found with ID: " + id);
            }

            Movie movie = snapshot.toObject(Movie.class);
            MovieDTO movieDTO = new MovieDTO();
            BeanUtils.copyProperties(movie, movieDTO);
            movieDTO.setActors(personService.findALLActorForMovie(id, "Movie_Actor"));
            movieDTO.setDirectors(personService.findALLActorForMovie(id, "Movie_Director"));
            movieDTO.setVideos(videoService.getAllVideosForMovie(id));
            movieDTO.setGenres(genresService.getAllForMovie(id));
            return movieDTO;
        } catch (ResourceNotFoundException e) {
            throw e; // Truyền lại ngoại lệ
        } catch (Exception e) {
            logger.error("Error fetching movie with ID: {}", id, e);
            throw new RuntimeException("Failed to fetch movie: " + e.getMessage(), e);
        }
    }

    public Page<Movie> searchMovies(String title, List<String> genres, List<Integer> years,
            List<String> nations, double minRating, Pageable pageable) throws Exception {
        CollectionReference movieRef = db.collection("Movies");
        Query query = movieRef;

        query = applyTitleFilter(query, title);
        query = applyRatingFilter(query, minRating);
        query = applyNationFilter(query, nations);
        query = applyYearFilter(query, years);
        query = applyGenresFilter(query, genres);

        // Áp dụng phân trang
        query = query.limit(pageable.getPageSize()).offset((int) pageable.getOffset());
        QuerySnapshot querySnapshot = query.get().get();
        List<Movie> movies = querySnapshot.getDocuments().stream()
                .map(doc -> {
                    Movie movie = doc.toObject(Movie.class);
                    movie.setMovie_Id(doc.getId());
                    return movie;
                })
                .collect(Collectors.toList());

        // Ước tính tổng số bản ghi (có thể cần truy vấn riêng để đếm chính xác)
        long total = db.collection("Movies").get().get().size(); // Cải thiện nếu cần
        return new PageImpl<>(movies, pageable, total);
    }

    public Page<Movie> searchWithMeili(String keyword, Pageable pageable) {
        Index index = meiliClient.index("movies");
        SearchResult result = index.search(keyword);
        List<String> ids = result.getHits().stream()
                .map(hit -> hit.get("movie_Id").toString())
                .collect(Collectors.toList());

        List<DocumentReference> refs = ids.stream()
                .map(id -> db.collection("Movies").document(id))
                .collect(Collectors.toList());

        try {
            List<DocumentSnapshot> snapshots = db.getAll(refs.toArray(new DocumentReference[0])).get();
            List<Movie> movies = snapshots.stream()
                    .map(snap -> snap.toObject(Movie.class))
                    .collect(Collectors.toList());

            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), movies.size());
            List<Movie> pagedMovies = movies.subList(start, end);

            return new PageImpl<>(pagedMovies, pageable, movies.size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Khôi phục trạng thái gián đoạn của luồng
            throw new RuntimeException("Luồng bị gián đoạn khi lấy dữ liệu từ Firestore", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Lỗi khi lấy dữ liệu từ Firestore", e);
        }
    }

    private Query applyTitleFilter(Query query, String title) {
        if (StringUtils.hasText(title)) {
            return query.whereGreaterThanOrEqualTo("title", title)
                    .whereLessThanOrEqualTo("title", title + "\uf8ff");
        }
        return query;
    }

    private Query applyRatingFilter(Query query, double minRating) {
        if (minRating > 0) {
            return query.whereGreaterThanOrEqualTo("rating", minRating);
        }
        return query;
    }

    private Query applyNationFilter(Query query, List<String> nations) {
        if (!CollectionUtils.isEmpty(nations)) {
            return query.whereIn("nation", nations);
        }
        return query;
    }

    private Query applyYearFilter(Query query, List<Integer> years) {
        if (!CollectionUtils.isEmpty(years)) {
            List<Timestamp> yearTimestamps = years.stream()
                    .flatMap(year -> {
                        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
                        LocalDateTime end = start.plusYears(1);
                        return Stream.of(
                                Timestamp.of(Date.from(start.atZone(ZoneId.systemDefault()).toInstant())),
                                Timestamp.of(Date.from(end.atZone(ZoneId.systemDefault()).toInstant())));
                    })
                    .collect(Collectors.toList());
            return query.whereIn("created_at", yearTimestamps);
        }
        return query;
    }

    private Query applyGenresFilter(Query query, List<String> genres) throws Exception {
        if (!CollectionUtils.isEmpty(genres)) {
            Query genreQuery = db.collection("Movie_Genres").whereIn("genres_id", genres);
            QuerySnapshot snapshot = genreQuery.get().get();
            List<String> movieIds = snapshot.getDocuments().stream()
                    .map(doc -> doc.getString("movie_id"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (movieIds.isEmpty()) {
                return query.whereEqualTo(FieldPath.documentId(), "none"); // Không có kết quả
            }
            return query.whereIn(FieldPath.documentId(), movieIds);
        }
        return query;
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
                return videoService.getAllVideosForMovie(movieId);
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