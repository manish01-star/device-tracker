package com.manish.device_tracker_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateDeviceRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    private Double latitude;

    private Double longitude;

    private Integer battery;

    private String address;
}