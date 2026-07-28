package com.manish.device_tracker_api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VideoResponse {

    private Long id;

    private String deviceId;

    private String videoName;

    private String videoUrl;

    private Long videoSize;

    private Long duration;      // seconds or milliseconds

    private String bucketId;

    private String folderName;

    private String mimeType;

    private LocalDateTime createdAt;
}