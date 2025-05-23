package com.appxemphim.firebaseBackend.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReviewRequest {
    private String Movie_id;
    private int  Rating;
}
