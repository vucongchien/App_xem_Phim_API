package com.appxemphim.firebaseBackend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.appxemphim.firebaseBackend.dto.response.FavoriteDTO;
import com.appxemphim.firebaseBackend.dto.response.MovieDTO;
import com.appxemphim.firebaseBackend.model.Favourite;
import com.appxemphim.firebaseBackend.model.Movie;
import com.appxemphim.firebaseBackend.security.JwtUtil;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavouriteService {
    private final HttpServletRequest request;
    private final Firestore db = FirestoreClient.getFirestore();
    private final JwtUtil jwtUtil;
    private final MovieService movieService;

    public String create(String movie_id){
        try{
            String token = request.getHeader("Authorization");
            if(token!=null && token.startsWith("Bearer ")){
                token= token.substring(7);
            }else{
                throw new RuntimeException("Token không hợp lệ");
            }
            String uid = jwtUtil.getUid(token);
            Timestamp timestamp = Timestamp.now();
            DocumentReference docRef = db.collection("Favourite_List").document(uid);

            Favourite favourite = new Favourite();
            favourite.setMovie_id(movie_id);
            favourite.setTime_add(timestamp);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                List<Map<String, Object>> rawList = (List<Map<String, Object>>) document.get("Videos");
                List<Favourite> favourites = new ArrayList<>();
            
                if (rawList != null) {
                    for (Map<String, Object> map : rawList) {
                        Favourite fav = new Favourite();
                        fav.setMovie_id((String) map.get("movie_id"));
                        fav.setTime_add((Timestamp) map.get("time_add"));
                        favourites.add(fav);
                    }
            
                    // Kiểm tra movie_id đã tồn tại chưa
                    boolean isExists = favourites.stream()
                            .anyMatch(f -> movie_id.equals(f.getMovie_id()));
            
                    if (isExists) {
                        rawList.removeIf(f -> movie_id.equals(f.get("movie_id")));
                        docRef.update("Videos", rawList);
                        return "Đã xóa khỏi danh sách yêu thích";
                    }
                }
            
                favourites.add(favourite);
                ApiFuture<WriteResult> writeResult = docRef.update("Videos", favourites);
                writeResult.get();
            
            } else {
                List<Favourite> favourites = new ArrayList<>();
                favourites.add(favourite);
                ApiFuture<WriteResult> writeResult = docRef.set(Collections.singletonMap("Videos", favourites));
                writeResult.get();
            }
            return "Thêm danh sách yêu thách thành công";

        }catch(Exception e){
            throw new RuntimeException("Lỗi khi thêm danh sách yêu thích: "+e.getMessage());
        }
    }

    public List<FavoriteDTO> getAllForUID(){
        try{
            String token = request.getHeader("Authorization");
            if(token!=null && token.startsWith("Bearer ")){
                token= token.substring(7);
            }else{
                throw new RuntimeException("Token không hợp lệ");
            }
            String uid = jwtUtil.getUid(token);
            DocumentReference docRef = db.collection("Favourite_List").document(uid);
            DocumentSnapshot snapshot = docRef.get().get();

            if (snapshot.exists()) {
                List<Map<String, Object>> videoList = (List<Map<String, Object>>) snapshot.get("Videos");

                List<Favourite> favourites = new ArrayList<>();
                if (videoList != null) {
                    for (Map<String, Object> map : videoList) {
                        Favourite fav = new Favourite();
                        fav.setTime_add((Timestamp)map.get("time_add"));
                        fav.setMovie_id((String) map.get("movie_id"));
                        favourites.add(fav);
                    }
                }

                // Tạo danh sách FavoriteListDTO
            List<FavoriteDTO> favoriteListDTOs = new ArrayList<>();
            for (Favourite fav : favourites) {
                Movie movieDTO = movieService.getMovieModelById(fav.getMovie_id());
                FavoriteDTO dto = new FavoriteDTO();
                dto.setMovie(movieDTO);
                dto.setTime_add(fav.getTime_add());
                favoriteListDTOs.add(dto);
            }

            return favoriteListDTOs;
        } else {
            throw new RuntimeException("Không có danh sách yêu thích");
        }
        }catch(Exception e){
            throw new RuntimeException("Lỗi khi lấy Danh sách yêu thích: "+ e.getMessage());
        }
    }
}
