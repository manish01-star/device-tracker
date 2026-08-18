package com.manish.device_tracker_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsSyncRequestDTO {

    /**
     * Device ID
     */
    private String deviceId;

    /**
     * Format:
     * yyyy-MM-dd
     */
    private String fromDate;

    /**
     * Format:
     * yyyy-MM-dd
     */
    private String toDate;
}