package com.appxemphim.firebaseBackend.service;

import java.util.HashMap;
import java.util.Map;

import org.checkerframework.checker.units.qual.g;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.appxemphim.firebaseBackend.Utilities.GoogleUtilities;
import com.appxemphim.firebaseBackend.dto.request.ProfileRequest;
import com.appxemphim.firebaseBackend.exception.ResourceNotFoundException;
import com.appxemphim.firebaseBackend.security.JwtUtil;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final GoogleUtilities googleUtilities;
    private final Firestore db = FirestoreClient.getFirestore();
    private final HttpServletRequest request;
    private final JwtUtil jwtUtil;
    
    public ResponseEntity<String> updateProfile(ProfileRequest profileRequest){
        try{
            String token = request.getHeader("Authorization");
            if(token!=null && token.startsWith("Bearer ")){
                token= token.substring(7);
            }else{
                throw new RuntimeException("Token không hợp lệ");
            }
            String uid = jwtUtil.getUid(token);
            StringBuilder responseMessage = new StringBuilder("Đã cập nhật: ");
            //String uid =profileRequest.getName();
            if (profileRequest.getFile() != null) {
                String uri = googleUtilities.uploadImage(profileRequest.getFile());
                DocumentReference docRef = db.collection("Avatar").document(uid);
                DocumentSnapshot snapshot = docRef.get().get();

                if (!snapshot.exists()) {
                    // Nếu chưa có document, tạo mới với trường avatar
                    Map<String, Object> data = new HashMap<>();
                    data.put("avatar", uri);
                    docRef.set(data);
                } else {
                    // Nếu đã có document, cập nhật trường avatar
                    docRef.update("avatar", uri);
                }
                responseMessage.append("avatar, ");
            }
            UserRecord.UpdateRequest updateRequest = new UserRecord.UpdateRequest(uid);
            boolean shouldUpdate = false;

            if (profileRequest.getGmail() != null && !profileRequest.getGmail().isEmpty()) {
                updateRequest.setEmail(profileRequest.getGmail());
                responseMessage.append("email, ");
                shouldUpdate = true;
            }

            if (profileRequest.getName() != null && !profileRequest.getName().isEmpty()) {
                updateRequest.setDisplayName(profileRequest.getName());
                responseMessage.append("tên, ");
                shouldUpdate = true;
            }

            // Cập nhật Firebase nếu có thay đổi
            if (shouldUpdate) {
                FirebaseAuth.getInstance().updateUser(updateRequest);
            }

            // Xoá dấu phẩy cuối cùng nếu có
            String finalMessage = responseMessage.toString().replaceAll(",\\s*$", "");
            return ResponseEntity.ok(finalMessage);
        } catch( Exception e ){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi cập nhật hồ sơ: " + e.getMessage());
        }
    }
}
