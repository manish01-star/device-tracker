package com.manish.device_tracker_api.dto;

import lombok.Data;

@Data
public class ImageRefreshRequest {

    private String deviceId;

    private String bucketId;

    private Integer limit;

    private Integer offset;

    private String order;
}
