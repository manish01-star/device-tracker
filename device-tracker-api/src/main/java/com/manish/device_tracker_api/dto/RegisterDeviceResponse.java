package com.manish.device_tracker_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterDeviceResponse {

    private Long id;
    private String username;
    private String deviceId;
    private String deviceModel;
    private String appVersion;
}