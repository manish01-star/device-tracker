package com.manish.device_tracker_api.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CallHistoryRequest {

    private String deviceId;

    private LocalDate fromDate;

    private LocalDate toDate;
}