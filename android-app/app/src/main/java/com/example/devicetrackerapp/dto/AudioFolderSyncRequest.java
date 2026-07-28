package com.example.devicetrackerapp.dto;

import java.util.List;

public class AudioFolderSyncRequest {

    private String deviceId;

    private List<AudioFolderPayload> folders;

    public AudioFolderSyncRequest() {
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<AudioFolderPayload> getFolders() {
        return folders;
    }

    public void setFolders(List<AudioFolderPayload> folders) {
        this.folders = folders;
    }
}