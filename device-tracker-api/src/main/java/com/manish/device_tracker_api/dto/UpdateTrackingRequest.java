package com.manish.device_tracker_api.dto;

import lombok.Data;

@Data
public class UpdateTrackingRequest {

    private String deviceId;

    private Boolean trackingEnabled;

    private Integer trackingInterval;

}