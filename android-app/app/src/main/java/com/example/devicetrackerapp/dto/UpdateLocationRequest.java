package com.example.devicetrackerapp.dto;

public class UpdateLocationRequest {

    private String deviceId;
    private Double latitude;
    private Double longitude;
    private Integer battery;
    private String address;

    public UpdateLocationRequest() {
    }

    public UpdateLocationRequest(String deviceId,
                                 Double latitude,
                                 Double longitude,
                                 Integer battery,
                                 String address) {
        this.deviceId = deviceId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.battery = battery;
        this.address = address;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getBattery() {
        return battery;
    }

    public void setBattery(Integer battery) {
        this.battery = battery;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}