package com.manish.device_tracker_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterDeviceRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotBlank(message = "Device model is required")
    private String deviceModel;

    @NotBlank(message = "App version is required")
    private String appVersion;
}