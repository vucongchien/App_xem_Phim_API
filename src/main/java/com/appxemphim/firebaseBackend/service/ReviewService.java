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
import com.appxemphim.firebaseBackend.security.JwtUtil;
import com.google.api.core.ApiFuture;
import com.google.cloud.Date;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
   private final Firestore db = FirestoreClient.getFirestore();
   private final AccountService accountService;
   private final JwtUtil jwtUtil;
   private final HttpServletRequest request;

   public String create(ReviewRequest reviewRequest){
      try{
         String token = request.getHeader("Authorization");
         if(reviewRequest.getMovie_id()==null){
               throw new RuntimeException("Lỗi movie null");
         }
         if(token!=null && token.startsWith("Bearer ")){
               token= token.substring(7);
         }else{
               throw new RuntimeException("Token không hợp lệ");
         }
         String uid = jwtUtil.getUid(token);
         Review review = new Review();
         review.setRating(reviewRequest.getRating());
         review.setCreated_at(Timestamp.now());
         review.setUid(uid);
         
         DocumentReference movieRef  = db.collection("Movies").document(reviewRequest.getMovie_id());
         ApiFuture<WriteResult> add = movieRef.update("reviews", FieldValue.arrayUnion(review));
         add.get();

         DocumentSnapshot snapshot = movieRef.get().get();
         List<Object> reviews = (List<Object>) snapshot.get("reviews");
         int quantity = (reviews != null) ? reviews.size() : 1;
         
         Double rating = snapshot.getDouble("rating");
         if (rating == null) rating = 0.0;
         rating = ((rating*(quantity-1))+reviewRequest.getRating())/quantity;
         
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
