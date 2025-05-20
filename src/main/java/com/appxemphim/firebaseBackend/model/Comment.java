package com.appxemphim.firebaseBackend.model;

import com.google.cloud.Timestamp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Comment {
    String User_id;
    String Content;
    String Created_at;
    String Parent_comment_id;
    int like;
}
