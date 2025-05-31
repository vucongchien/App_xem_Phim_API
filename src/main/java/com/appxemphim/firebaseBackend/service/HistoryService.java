package com.appxemphim.firebaseBackend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.checkerframework.checker.units.qual.h;
import org.springframework.stereotype.Service;

import com.appxemphim.firebaseBackend.dto.request.HistoryRequest;
import com.appxemphim.firebaseBackend.model.Favourite;
import com.appxemphim.firebaseBackend.model.History;
import com.appxemphim.firebaseBackend.security.JwtUtil;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HttpServletRequest request;
    private final Firestore db = FirestoreClient.getFirestore();
    private final JwtUtil jwtUtil;

    public String create(HistoryRequest hitstoryRequest){
        try{
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            } else {
                throw new RuntimeException("Token không hợp lệ");
            }
            String uid = jwtUtil.getUid(token);
            Timestamp timestamp = Timestamp.now();
            DocumentReference docRef = db.collection("History").document(uid);


            History history = new History();
            history.setPerson_view(hitstoryRequest.getPerson_view());
            history.setVideo_id(hitstoryRequest.getVideo_id());
            history.setUpdated_at(timestamp);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                List<History> videos = (List<History>) document.get("videos");
                if (videos == null) {
                    videos = new ArrayList<>();
                }
                videos.add(history);
                ApiFuture<WriteResult> writeResult = docRef.update("videos", videos);
                writeResult.get();
            } else {
                List<History> videos = new ArrayList<>();
                videos.add(history);

                ApiFuture<WriteResult> writeResult = docRef.set(Collections.singletonMap("videos", videos));
                writeResult.get();
            }
            return "Thêm lịch sử thành công!";
        }catch(Exception e){
            throw new RuntimeException("Lỗi khi thêm lịch sử: "+ e.getMessage());
        }
    }

    public List<History> findAllForUID(){
        try{
            String token = request.getHeader("Authorization");
            if(token!=null && token.startsWith("Bearer ")){
                token= token.substring(7);
            }else{
                throw new RuntimeException("Token không hợp lệ");
            }
            String uid = jwtUtil.getUid(token);
            DocumentReference docRef = db.collection("History").document(uid);
            DocumentSnapshot snapshot = docRef.get().get();

            if (snapshot.exists()) {
                List<Map<String, Object>> videoList = (List<Map<String, Object>>) snapshot.get("videos");
                List<History> history = new ArrayList<>();
                if (videoList != null) {
                    for (Map<String, Object> map : videoList) {
                        History his = new History();
                       Object personViewObj = map.get("person_view");
                        if (personViewObj instanceof Number) {
                            his.setPerson_view(((Number) personViewObj).doubleValue());
                        }
                        his.setVideo_id((String) map.get("video_id") );
                        his.setUpdated_at((Timestamp) map.get("updated_at"));
                        history.add(his);
                    }
                }
                return history;
        } else {
            return Collections.emptyList();
        }
        }catch(Exception e){
            throw new RuntimeException("Lỗi khi lấy Danh sách yêu thích: "+ e.getMessage());
        }
    }
    
}
