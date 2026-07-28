package com.manish.device_tracker_api.dto;

import lombok.Data;

@Data
public class VideoFolderPayload {

    private String bucketId;

    private String folderName;

    private Integer videoCount;

}