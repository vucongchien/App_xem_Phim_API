package com.appxemphim.firebaseBackend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.appxemphim.firebaseBackend.dto.request.ReviewRequest;
import com.appxemphim.firebaseBackend.dto.response.PersonReviewDTO;
import com.appxemphim.firebaseBackend.exception.ResourceNotFoundException;
import com.appxemphim.firebaseBackend.model.Review;
import com.appxemphim.firebaseBackend.model.Video;
import com.google.api.core.ApiFuture;
import com.google.cloud.Date;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final Firestore db = FirestoreClient.getFirestore();
    private final AccountService accountService;


    public String create(ReviewRequest request){
         try{
            Review review = new Review();
            PersonReviewDTO personReviewDTO = accountService.getInformation(request.getUser_id());
            review.setAvatar(personReviewDTO.getAvatar());
            review.setUserName(personReviewDTO.getName());
            review.setRating(request.getRating());
            review.setDescription(request.getDescription());
            review.setCreated_at(Timestamp.now());
            
            DocumentReference movieRef  = db.collection("Movies").document(request.getMovie_id());
            ApiFuture<WriteResult> add = movieRef.update("reviews", FieldValue.arrayUnion(review));
            add.get();

            DocumentSnapshot snapshot = movieRef.get().get();
            List<Object> reviews = (List<Object>) snapshot.get("reviews");
            int quantity = (reviews != null) ? reviews.size() : 1;
            
            Double rating = snapshot.getDouble("rating");
            if (rating == null) rating = 0.0;
            rating = ((rating*(quantity-1))+request.getRating())/quantity;
            
            ApiFuture<WriteResult> future = movieRef.update("rating", rating);
            future.get();
            return "Thêm review thành công";
         }catch( Exception e){
            return "Thêm review thất bại: "+ e.getMessage();
         }
    }

    public List<Review> getallReviewForMovie(String movie_id){
      try{
         DocumentReference docRef = db.collection("Movies").document(movie_id.trim());
         DocumentSnapshot snapshot = docRef.get().get();
         if (!snapshot.exists()) {
                  throw new ResourceNotFoundException("Movie not found with ID: " + movie_id);
               }
         List<Review>  reviews =(List<Review>) snapshot.get("reviews");
         return reviews;
      }catch ( Exception e){
         throw new RuntimeException("Lỗi khi lấy review: "+ e);
      }
      
    }

    
}
