package com.manish.device_tracker_api.dto;

import lombok.Data;

@Data
public class ScreenStatusRequest {

    private String deviceId;

    private String status;

}
