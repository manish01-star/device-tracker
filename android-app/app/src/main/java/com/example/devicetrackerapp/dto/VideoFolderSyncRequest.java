package com.example.devicetrackerapp.dto;

import java.util.List;

public class VideoFolderSyncRequest {

    private String deviceId;

    private List<VideoFolderPayload> folders;

    public VideoFolderSyncRequest() {
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<VideoFolderPayload> getFolders() {
        return folders;
    }

    public void setFolders(List<VideoFolderPayload> folders) {
        this.folders = folders;
    }
}