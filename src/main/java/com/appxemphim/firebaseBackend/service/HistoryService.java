package com.appxemphim.firebaseBackend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    public String create(HistoryRequest historyRequest) {
    try {
        // Lấy và kiểm tra token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Token không hợp lệ");
        }
        token = token.substring(7);

        // Giải mã token
        String uid = jwtUtil.getUid(token);
        Timestamp timestamp = Timestamp.now();
        DocumentReference docRef = db.collection("History").document(uid);

        // Tạo object History mới
        History newHistory = new History();
        newHistory.setPerson_view(historyRequest.getPerson_view());
        newHistory.setVideo_id(historyRequest.getVideo_id());
        newHistory.setUpdated_at(timestamp);

        // Lấy dữ liệu hiện tại từ Firestore
        DocumentSnapshot document = docRef.get().get();
        List<History> videos = new ArrayList<>();

        if (document.exists()) {
            List<Map<String, Object>> videoList = (List<Map<String, Object>>) document.get("videos");
            if (videoList != null) {
                for (Map<String, Object> map : videoList) {
                    History history = new History();
                    Object personViewObj = map.get("person_view");
                    if (personViewObj instanceof Number) {
                        history.setPerson_view(((Number) personViewObj).doubleValue());
                    }
                    history.setVideo_id((String) map.get("video_id"));
                    history.setUpdated_at((Timestamp) map.get("updated_at"));
                    videos.add(history);
                }
            }

            // Kiểm tra video đã tồn tại chưa
            boolean found = false;
            for (int i = 0; i < videos.size(); i++) {
                if (Objects.equals(videos.get(i).getVideo_id(), newHistory.getVideo_id())) {
                    videos.set(i, newHistory);
                    found = true;
                    break;
                }
            }
            if (!found) {
                videos.add(newHistory);
            }
            // Cập nhật danh sách vào Firestore
            docRef.update("videos", videos).get();
        } else {
            // Nếu chưa tồn tại document, tạo mới
            videos.add(newHistory);
            docRef.set(Collections.singletonMap("videos", videos)).get();
        }

        return "Thêm hoặc cập nhật lịch sử thành công!";
    } catch (Exception e) {
        throw new RuntimeException("Lỗi khi thêm lịch sử: " + e.getMessage(), e);
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
