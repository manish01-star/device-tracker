package com.manish.device_tracker_api.dto;

import lombok.Data;

@Data
public class CameraRequest {

    private String deviceId;

    private String cameraType;
}
