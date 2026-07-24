package com.example.devicetrackerapp.dto;

import android.net.Uri;

public class ImageItem {

    private String imageName;

    private Uri imageUri;

    private long imageSize;

    private String bucketId;

    private String folderName;

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

    public Uri getImageUri() {
        return imageUri;
    }

    public long getImageSize() {
        return imageSize;
    }

    public String getBucketId() {
        return bucketId;
    }

    public String getFolderName() {
        return folderName;
    }

    public ImageItem() {
    }

}