package com.manish.device_tracker_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsSyncPayload {

    /**
     * Device sending SMS data.
     */
    private String deviceId;

    /**
     * SMS list received from Android.
     */
    private List<SmsDTO> smsList;
}