package com.appxemphim.firebaseBackend.service;

import org.springframework.stereotype.Service;

import com.appxemphim.firebaseBackend.dto.request.CommentRequest;
import com.appxemphim.firebaseBackend.model.Comment;
import com.appxemphim.firebaseBackend.security.JwtUtil;
import com.google.cloud.Timestamp;
import com.google.firebase.database.DatabaseReference;

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
            Comment comment = new Comment();
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
}
