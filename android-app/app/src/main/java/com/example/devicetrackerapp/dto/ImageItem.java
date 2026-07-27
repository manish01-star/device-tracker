package com.example.devicetrackerapp.dto;

import android.net.Uri;

public class ImageItem {

    private String imageName;

    private Uri imageUri;

    private long imageSize;

    private String bucketId;

    private String folderName;

    public ImageItem() {
    }

    public ImageItem(String imageName,
                     Uri imageUri,
                     long imageSize,
                     String bucketId,
                     String folderName) {

        this.imageName = imageName;
        this.imageUri = imageUri;
        this.imageSize = imageSize;
        this.bucketId = bucketId;
        this.folderName = folderName;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public long getImageSize() {
        return imageSize;
    }

    public void setImageSize(long imageSize) {
        this.imageSize = imageSize;
    }

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
}