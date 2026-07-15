package com.manish.device_tracker_api.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DeviceLocationHistoryResponse {

    private Double latitude;

    private Double longitude;

    private String address;

    private Integer battery;

    private LocalDateTime createdAt;

}