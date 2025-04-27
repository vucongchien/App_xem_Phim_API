package com.appxemphim.firebaseBackend.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.appxemphim.firebaseBackend.dto.request.SetPassWordRequest;
import com.appxemphim.firebaseBackend.dto.response.PersonReviewDTO;
import com.appxemphim.firebaseBackend.exception.ResourceNotFoundException;
import com.appxemphim.firebaseBackend.security.JwtUtil;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;


import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;


@Service
public class AccountService {
    private final Firestore db = FirestoreClient.getFirestore();
    @Autowired
    private JwtUtil jwtUtil;
    private FirebaseAuth firebaseAuth;



    public String createToken(String uid ) {
        try{        
            DocumentReference roleRef = db.collection("Account_role").document(uid);
            DocumentSnapshot snapshot = roleRef.get().get();
            String roleId;
            if (!snapshot.exists() || snapshot.get("role") == null) {
                Map<String, Object> defaultRole = new HashMap<>();
                defaultRole.put("role", "2");
                roleRef.set(defaultRole); // cập nhật vào Firestore
                roleId = "2";
            } else {
                roleId = String.valueOf(snapshot.get("role"));
            }
            DocumentSnapshot roleSnapshot = db.collection("Role").document(roleId).get().get();
            if (!roleSnapshot.exists()) {
                throw new RuntimeException("Role " + roleId + " not found in Role collection.");
            }
            String role = roleSnapshot.getString("name");
            return jwtUtil.createJwtToken(uid, role);
        }catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public String setPass(SetPassWordRequest request) {
        try {
            String email = request.getEmail();
            String newPassword = request.getNewpass();

            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
            if(userRecord==null){
                throw new RuntimeException("Khong tìm thấy tài khoản với email này");
            }
            String uid = userRecord.getUid();
            UserRecord.UpdateRequest updateRequest = new UserRecord.UpdateRequest(uid).setPassword(newPassword);
            firebaseAuth.getInstance().updateUser(updateRequest);

            return "Mật khẩu đã được cập nhật thành công.";
        } catch (Exception e) {
            e.printStackTrace();
           throw new RuntimeException("Lỗi khi thay đổi mật khẩu: "+ e.getMessage());
        }
    }

    public PersonReviewDTO getInformation(String uid){
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);
            if(userRecord==null){
                throw new RuntimeException("Khong tìm thấy tài khoản với email này");
            }
            String name = userRecord.getDisplayName();
            DocumentReference docRef = db.collection("Avatar").document(uid);
            DocumentSnapshot snapshot = docRef.get().get();
            if (!snapshot.exists()) {
                  throw new ResourceNotFoundException("Movie not found with ID: " + uid);
               }
            String avatarUrl = snapshot.get("avatar").toString();
            if(avatarUrl==null || avatarUrl== ""){
                avatarUrl= "linkanhdefaul";
            }
            return new PersonReviewDTO(avatarUrl,name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
