package com.appxemphim.firebaseBackend.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PersonReviewDTO {
    String avatar;
    String name;
    String email;

    public PersonReviewDTO( String avatar, String name, String email){
        this.avatar= avatar;
        this.name= name;
        this.email = email;
    }
}
