package com.example.devicetrackerapp.dto;

public class ImageFolderItem {

    private String bucketId;

    private String folderName;

    private Integer imageCount;

    public String getBucketId() {
        return bucketId;
    }

    public void setBucketId(String bucketId) {
        this.bucketId = bucketId;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public Integer getImageCount() {
        return imageCount;
    }

    public void setImageCount(Integer imageCount) {
        this.imageCount = imageCount;
    }

    public ImageFolderItem() {
    }

    public ImageFolderItem(String bucketId,
                           String folderName,
                           Integer imageCount) {

        this.bucketId = bucketId;
        this.folderName = folderName;
        this.imageCount = imageCount;
    }

}