package com.appxemphim.firebaseBackend.model;

import java.time.LocalDate;

import com.google.cloud.Date;
import com.google.cloud.Timestamp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Review {
    private String userName;
    private String avatar;
    private int Rating;
    private String Description;
    private Timestamp Created_at;
}
