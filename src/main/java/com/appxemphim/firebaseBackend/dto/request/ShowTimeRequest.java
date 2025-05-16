package com.appxemphim.firebaseBackend.dto.request;

import java.util.List;

import javax.validation.constraints.NotBlank;

import com.appxemphim.firebaseBackend.model.EpisodeInfo;

import lombok.Data;


@Data
public class ShowTimeRequest {
    @NotBlank(message = "Movie ID is required")
    private String movieId;

    @NotBlank(message = "thông tin tập chiếu tiếp theotheo is required")
    private List<EpisodeInfoDTO> showTimes; // Danh sách suất chiếu
}
