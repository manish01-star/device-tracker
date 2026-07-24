package com.example.devicetrackerapp.dto;

import java.util.List;

public class ImageFolderSyncRequest {

    private String deviceId;

    private List<ImageFolderItem> folders;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<ImageFolderItem> getFolders() {
        return folders;
    }

    public void setFolders(List<ImageFolderItem> folders) {
        this.folders = folders;
    }
}
