package com.manish.device_tracker_api.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ImageFolderResponse {

    private String bucketId;

    private String folderName;

    private Integer imageCount;
}
