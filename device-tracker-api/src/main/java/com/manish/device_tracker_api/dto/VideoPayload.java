package com.manish.device_tracker_api.dto;

import lombok.Data;

@Data
public class VideoPayload {

    private String deviceId;

    private String videoName;

    private String videoPath;

    private Long videoSize;

    private Long duration;
}
