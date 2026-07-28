package com.manish.device_tracker_api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VideoFolderResponse {

    private String bucketId;

    private String folderName;

    private Integer videoCount;

}