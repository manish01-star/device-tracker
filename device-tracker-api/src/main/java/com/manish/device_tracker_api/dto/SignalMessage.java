package com.manish.device_tracker_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalMessage {

    private String deviceId;

    // offer | answer | candidate
    private String type;

    // SDP
    private String sdp;

    // ICE Candidate
    private String candidate;

    private String sdpMid;

    private Integer sdpMLineIndex;

}