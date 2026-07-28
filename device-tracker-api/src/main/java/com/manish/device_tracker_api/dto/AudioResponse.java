package com.manish.device_tracker_api.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioResponse {

    private Long id;

    private String audioName;

    private String audioUrl;

    private Long audioSize;

    private Long duration;

    private LocalDateTime createdAt;

}