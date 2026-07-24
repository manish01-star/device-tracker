package com.manish.device_tracker_api.dto;

import lombok.Data;

@Data
public class ImageSyncRequest {

    private String deviceId;
    private String imageFolder;
    private Integer imageLimit;
    private Integer imageOffset;
    private String imageOrder;

}