package com.manish.device_tracker_api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrackingConfigResponse {

    private Boolean trackingEnabled;

    private Integer trackingInterval;

}
