package com.appxemphim.firebaseBackend.dto.response;

import java.util.List;

import com.appxemphim.firebaseBackend.model.Genres;
import com.appxemphim.firebaseBackend.model.Person;
import com.appxemphim.firebaseBackend.model.Review;
import com.appxemphim.firebaseBackend.model.Video;
import com.google.cloud.Timestamp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MovieDTO {
    private String movie_Id;
    private String title;           //tên của bộ phim
    private String description;     // mô tả của bộ phim
    private String poster_url;      // link ảnh poster
    private String trailer_url;     // link trailer của bộ phim
    private double rating;           // đánh giá chung của phim
    private String nation;          // bộ phim thuộc quốc gia nào
    private Timestamp  created_at;        // thời gian tạo phim
    private List<Video> videos;
    private List<Person> actors;
    private List<Person> directors;
    private List<Genres> genres;
}
