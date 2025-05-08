package com.appxemphim.firebaseBackend.dto.response;

import com.appxemphim.firebaseBackend.model.Movie;
import com.google.cloud.Timestamp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FavoriteDTO {
    Movie movie;
    Timestamp time_add;
}
