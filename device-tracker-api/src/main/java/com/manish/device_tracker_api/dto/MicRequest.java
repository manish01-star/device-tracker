package com.manish.device_tracker_api.dto;

import lombok.Data;

@Data
public class MicRequest {

    private String deviceId;
    private Integer duration;

}
