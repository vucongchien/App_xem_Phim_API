package com.appxemphim.firebaseBackend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.appxemphim.firebaseBackend.dto.request.CommentRequest;
import com.appxemphim.firebaseBackend.model.Comment;
import com.appxemphim.firebaseBackend.security.JwtUtil;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentRervice {
        private final JwtUtil jwtUtil;
        private final HttpServletRequest request;
        private final DatabaseReference database ;

    public String create(CommentRequest commentRequest){
        try{
            String token = request.getHeader("Authorization");
            if(commentRequest.getMovie_id()==null){
                throw new RuntimeException("Lỗi movie null");
            }
            if(token!=null && token.startsWith("Bearer ")){
                token= token.substring(7);
            }else{
                throw new RuntimeException("Token không hợp lệ");
            }
            String uid = jwtUtil.getUid(token);
            Comment comment = new Comment();
            comment.setContent(commentRequest.getContent());
            comment.setCreated_at(Timestamp.now());
            comment.setParent_comment_id(commentRequest.getParent_comment_id());
            comment.setUser_id(uid);
            database.child("Comment").child(commentRequest.getMovie_id()).push().setValueAsync(comment);
            return "Thêm bình luận thành công";
        }catch( Exception e){
            throw new RuntimeException("Lỗi khi thêm comment: "+ e.getMessage());
        }  
    }

    //đọc comment sẽ ở client
    //delete comment ở client
    

}
