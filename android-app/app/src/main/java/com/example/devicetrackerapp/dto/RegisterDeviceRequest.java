package com.example.devicetrackerapp.dto;

public class RegisterDeviceRequest {

    private String username;
    private String deviceId;
    private String deviceModel;
    private String appVersion;

    public RegisterDeviceRequest() {
    }

    public RegisterDeviceRequest(String username, String deviceId, String deviceModel, String appVersion) {
        this.username = username;
        this.deviceId = deviceId;
        this.deviceModel = deviceModel;
        this.appVersion = appVersion;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }
}