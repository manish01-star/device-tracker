package com.manish.device_tracker_api.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageResponse {

    private Long id;

    private String imageName;

    private String imageUrl;

    private Long imageSize;

    private LocalDateTime createdAt;
}