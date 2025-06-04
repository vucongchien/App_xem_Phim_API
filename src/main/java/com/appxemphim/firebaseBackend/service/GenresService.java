package com.appxemphim.firebaseBackend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.appxemphim.firebaseBackend.model.Genres;
import com.appxemphim.firebaseBackend.model.Movie;
import com.appxemphim.firebaseBackend.model.MovieGenres;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.FirebaseDatabase;


@Service
public class GenresService {

    private final Firestore db = FirestoreClient.getFirestore();
    
    public List<Genres> getAllForMovie(String moivie_id){
        List<Genres> result = new ArrayList<>();
        try{
            CollectionReference movieGenresRef = db.collection("Movie_Genres");
            Query query = movieGenresRef.whereEqualTo("movive_Id", moivie_id);
            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            for (DocumentSnapshot document : querySnapshot.get().getDocuments()){
                String genresId = document.getString("genres_id");
                DocumentReference genresRef = db.collection("Genres").document(genresId);
                ApiFuture<DocumentSnapshot> future = genresRef.get();
                DocumentSnapshot genresDoc = future.get();
                if(genresDoc.exists()){
                    Genres genres = genresDoc.toObject(Genres.class);
                    result.add(genres);
                }
            }
            return result;
        }catch ( Exception e){
            throw new RuntimeException("Lỗi khi lấy thể loại: "+ e.getMessage());
        }
    }

    public List<Genres> getAllFGenres(){
         List<Genres> genresList = new ArrayList<>();
    try {
        ApiFuture<QuerySnapshot> future = db.collection("Genres").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        for (QueryDocumentSnapshot doc : documents) {
            Genres genres = doc.toObject(Genres.class);
            genresList.add(genres);
        }
    } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException("Lỗi khi lấy danh sách: "+ e.getMessage()); 
    }
    return genresList;
    }

    public String addmovieGenres(MovieGenres movieGenres){
        try{
            MovieGenres mg = new MovieGenres();
            DocumentReference docRef = db.collection("Movie_Genres").document();
            mg.setGenres_id(movieGenres.getGenres_id());
            mg.setMovive_Id(movieGenres.getMovive_Id());
            docRef.set(mg).get();
            return "Thêm thể loại cho phim thành công!";
        }catch (Exception e){
        throw new RuntimeException("Lỗi khi thêm movieGenres: "+ e.getMessage());
        }
    }
    

}
