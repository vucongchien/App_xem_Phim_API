package com.appxemphim.firebaseBackend.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommentRequest {
    String movie_id;
    String parent_comment_id;
    String content;
}
