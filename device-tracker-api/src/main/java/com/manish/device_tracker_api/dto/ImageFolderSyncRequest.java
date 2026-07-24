package com.manish.device_tracker_api.dto;

import java.util.List;

import lombok.Data;

@Data
public class ImageFolderSyncRequest {

    private String deviceId;

    private List<ImageFolderPayload> folders;
}
