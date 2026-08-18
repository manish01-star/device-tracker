package com.manish.device_tracker_api.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CallHistorySyncRequest {

    private String deviceId;

    private LocalDate fromDate;

    private LocalDate toDate;

    private List<CallHistorySyncItemDTO> calls;
}