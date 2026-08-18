package com.manish.device_tracker_api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CallHistoryDTO {

    private Long id;

    private String deviceId;

    private String phoneNumber;

    private String callType;

    private LocalDateTime callDate;

    private Long duration;

    private String contactName;
}
