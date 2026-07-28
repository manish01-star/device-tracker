package com.manish.device_tracker_api.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MicRecordingRequest {

    private String deviceId;

    private Integer duration;

}