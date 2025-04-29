package com.appxemphim.firebaseBackend.service;

import java.util.ArrayList;
import java.util.List;

import javax.imageio.IIOException;

import org.springframework.stereotype.Service;

import com.appxemphim.firebaseBackend.Utilities.GoogleUtilities;
import com.appxemphim.firebaseBackend.dto.request.VideoRequest;
import com.appxemphim.firebaseBackend.model.Movie;
import com.appxemphim.firebaseBackend.model.Video;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final GoogleUtilities googleUtilities;
    private final Firestore db = FirestoreClient.getFirestore();

    public String addVideoToMovie(VideoRequest videoRequest) {
        try {

            // tạo video
            Video video = new Video();
            DocumentReference videoRef = db.collection("Video").document();

            video.setVideo_id(videoRef.getId());
            video.setDuration(googleUtilities.getVideoDuration(videoRequest.getLink()));
            video.setVideo_url(videoRequest.getLink());
            ApiFuture<com.google.cloud.firestore.WriteResult> future = videoRef.set(video);
            future.get();

            DocumentReference movieRef = db.collection("Movies").document(videoRequest.getMovie_id());
            ApiFuture<WriteResult> add = movieRef.update("videos", FieldValue.arrayUnion(video.getVideo_id()));
            add.get();

            // 🔥 3. Tìm user yêu thích phim này
            List<String> fcmTokens = new ArrayList<>();
            ApiFuture<QuerySnapshot> favoritesListQuery = db.collection("Favourite_List").get();
            List<QueryDocumentSnapshot> favoriteListDocs = favoritesListQuery.get().getDocuments();

            for (QueryDocumentSnapshot doc : favoriteListDocs) {
                List<String> likedMovies = (List<String>) doc.get("movie_ids");
                if (likedMovies != null && likedMovies.contains(videoRequest.getMovie_id())) {
                    // Lấy FCM token của user
                    String userId = doc.getId();
                    DocumentSnapshot userSnap = db.collection("Users").document(userId).get().get();
                    String token = userSnap.getString("fcm_token");
                    if (token != null && !token.isEmpty()) {
                        fcmTokens.add(token);
                    }
                }
            }

            // 🔥 4. Gửi thông báo FCM
            for (String token : fcmTokens) {
                sendFcmNotification(token, "Phim"+ movieRef.get().get().getString("title") + "bạn yêu thích vừa có tập mới!",movieRef.getId());
            }

            return "Thêm video thành công!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Thêm video thất bại: " + e.getMessage();
        }
    }

    public List<Video> getAllVideosForMovie(String Movie_id) {
        List<Video> result = new ArrayList<>();
        try {
            DocumentReference movieRef = db.collection("Movies").document(Movie_id);
            ApiFuture<DocumentSnapshot> future = movieRef.get();
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                List<String> videoIds = (List<String>) document.get("videos");
                for (String id : videoIds) {
                    DocumentReference docRef = db.collection("Video").document(id.trim());
                    DocumentSnapshot snapshot = docRef.get().get();
                    result.add(snapshot.toObject(Video.class));
                }
                return result;
            } else {
                throw new RuntimeException("Không thể lấy danh sách video: ");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    // hàm gửi thông báo
    private void sendFcmNotification(String token, String message,String Movie_id) {
        try {
            Message msg = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle("Cập nhật mới!")
                            .setBody(message)
                            .build())
                            .putData("moive_id", Movie_id)
                    .build();

            FirebaseMessaging.getInstance().send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
