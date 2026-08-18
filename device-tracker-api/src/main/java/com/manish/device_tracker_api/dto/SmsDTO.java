package com.manish.device_tracker_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsDTO {

    /**
     * Backend DB ID
     */
    private Long id;

    /**
     * Original Android SMS ID
     */
    private Long smsId;

    /**
     * Device ID
     */
    private String deviceId;

    /**
     * Contact name
     */
    private String contactName;

    /**
     * Phone number
     */
    private String phoneNumber;

    /**
     * SMS message
     */
    private String messageBody;

    /**
     * RECEIVED / SENT / DRAFT / FAILED
     */
    private String smsType;

    /**
     * Original SMS date/time
     */
    private LocalDateTime smsDate;

    /**
     * Read / Unread
     */
    private Boolean readStatus;

    /**
     * Android thread/conversation ID
     */
    private String threadId;
}