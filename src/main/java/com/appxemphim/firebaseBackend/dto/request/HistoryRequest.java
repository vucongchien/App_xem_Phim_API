package com.appxemphim.firebaseBackend.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HistoryRequest {
    String video_id;
    Double person_view;
}
