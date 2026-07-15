package com.example.devicetrackerapp.dto;

public class TrackingConfigResponse {

    private Boolean trackingEnabled;
    private Integer trackingInterval;

    public TrackingConfigResponse() {
    }

    public Boolean getTrackingEnabled() {
        return trackingEnabled;
    }

    public void setTrackingEnabled(Boolean trackingEnabled) {
        this.trackingEnabled = trackingEnabled;
    }

    public Integer getTrackingInterval() {
        return trackingInterval;
    }

    public void setTrackingInterval(Integer trackingInterval) {
        this.trackingInterval = trackingInterval;
    }
}