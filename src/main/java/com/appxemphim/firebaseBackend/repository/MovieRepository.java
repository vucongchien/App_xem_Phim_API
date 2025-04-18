package com.appxemphim.firebaseBackend.repository;

import com.appxemphim.firebaseBackend.model.Movie;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class MovieRepository {

    private final Firestore db = FirestoreClient.getFirestore();
    private static final String COLLECTION_NAME = "Movies";

    public String save(Movie movie) {
        try {
            DocumentReference docRef = db.collection(COLLECTION_NAME).document();
            movie.setMovie_Id(docRef.getId());
            docRef.set(movie).get();
            return movie.getMovie_Id();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save movie: " + e.getMessage(), e);
        }
    }

    public Optional<Movie> findById(String id) {
        try {
            DocumentSnapshot document = db.collection(COLLECTION_NAME).document(id).get().get();
            if (document.exists()) {
                Movie movie = document.toObject(Movie.class);
                movie.setMovie_Id(document.getId());
                return Optional.of(movie);
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch movie: " + e.getMessage(), e);
        }
    }

    public List<Movie> searchMovies(
            String title,
            List<String> genres,
            List<Integer> years,
            List<String> nations,
            double minRating) {
        try {
            CollectionReference movieRef = db.collection(COLLECTION_NAME);
            Query query = movieRef;

            // Title filter
            if (StringUtils.hasText(title)) {
                query = query.whereGreaterThanOrEqualTo("title", title)
                        .whereLessThanOrEqualTo("title", title + "\uf8ff");
            }

            // Rating filter
            if (minRating > 0) {
                query = query.whereGreaterThanOrEqualTo("rating", minRating);
            }

            // Year filter
            if (!CollectionUtils.isEmpty(years)) {
                List<Timestamp> yearTimestamps = new ArrayList<>();
                for (Integer year : years) {
                    LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
                    LocalDateTime end = start.plusYears(1);
                    yearTimestamps.add(Timestamp.of(Date.from(start.atZone(ZoneId.systemDefault()).toInstant())));
                    yearTimestamps.add(Timestamp.of(Date.from(end.atZone(ZoneId.systemDefault()).toInstant())));
                }
                query = query.whereIn("created_at", yearTimestamps);
            }

            // Nation filter
            if (!CollectionUtils.isEmpty(nations)) {
                query = query.whereIn("nation", nations);
            }

            // Genres filter
            if (!CollectionUtils.isEmpty(genres)) {
                List<String> movieIds = new ArrayList<>();
                Query genreQuery = db.collection("Movie_Genres")
                        .whereIn("genres_id", genres);
                QuerySnapshot snapshot = genreQuery.get().get();
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    String movieId = doc.getString("movie_id");
                    if (movieId != null) {
                        movieIds.add(movieId);
                    }
                }
                if (!movieIds.isEmpty()) {
                    query = query.whereIn(FieldPath.documentId(), movieIds);
                } else {
                    return new ArrayList<>();
                }
            }

            // Execute query
            QuerySnapshot querySnapshot = query.get().get();
            List<Movie> result = new ArrayList<>();
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Movie movie = document.toObject(Movie.class);
                movie.setMovie_Id(document.getId());
                result.add(movie);
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Search failed: " + e.getMessage(), e);
        }
    }
}