package com.example.devicetrackerapp.dto;

public class ImageItem {

    private String imageName;
    private String imagePath;
    private Long imageSize;

    public ImageItem() {
    }

    public ImageItem(String imageName,
                     String imagePath,
                     Long imageSize) {

        this.imageName = imageName;
        this.imagePath = imagePath;
        this.imageSize = imageSize;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Long getImageSize() {
        return imageSize;
    }

    public void setImageSize(Long imageSize) {
        this.imageSize = imageSize;
    }
}