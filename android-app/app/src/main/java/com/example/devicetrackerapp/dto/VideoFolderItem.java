package com.example.devicetrackerapp.dto;

public class VideoFolderItem {

    private String bucketId;
    private String folderName;
    private int videoCount;

    public VideoFolderItem(String bucketId,
                           String folderName,
                           int videoCount) {

        this.bucketId = bucketId;
        this.folderName = folderName;
        this.videoCount = videoCount;
    }

    public String getBucketId() {
        return bucketId;
    }

    public String getFolderName() {
        return folderName;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(int videoCount) {
        this.videoCount = videoCount;
    }
}