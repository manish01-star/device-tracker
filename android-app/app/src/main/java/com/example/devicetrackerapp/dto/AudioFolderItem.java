package com.example.devicetrackerapp.dto;

public class AudioFolderItem {

    private String bucketId;

    private String folderName;

    private int audioCount;

    public AudioFolderItem(
            String bucketId,
            String folderName,
            int audioCount) {

        this.bucketId = bucketId;
        this.folderName = folderName;
        this.audioCount = audioCount;
    }

    public String getBucketId() {
        return bucketId;
    }

    public String getFolderName() {
        return folderName;
    }

    public int getAudioCount() {
        return audioCount;
    }

    public void setAudioCount(int audioCount) {
        this.audioCount = audioCount;
    }
}