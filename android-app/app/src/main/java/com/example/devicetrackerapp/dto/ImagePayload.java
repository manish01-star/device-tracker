package com.example.devicetrackerapp.dto;

import java.util.List;

public class ImagePayload {

    private String deviceId;
    private List<ImageItem> images;

    public ImagePayload() {
    }

    public ImagePayload(String deviceId,
                        List<ImageItem> images) {

        this.deviceId = deviceId;
        this.images = images;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<ImageItem> getImages() {
        return images;
    }

    public void setImages(List<ImageItem> images) {
        this.images = images;
    }
}