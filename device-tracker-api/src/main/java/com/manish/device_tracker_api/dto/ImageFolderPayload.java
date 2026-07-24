package com.manish.device_tracker_api.dto;

import lombok.Data;

@Data
public class ImageFolderPayload {

    private String bucketId;

    private String folderName;

    private Integer imageCount;
}
