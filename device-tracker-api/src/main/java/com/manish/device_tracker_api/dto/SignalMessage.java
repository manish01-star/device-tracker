package com.manish.device_tracker_api.dto;

import com.manish.device_tracker_api.entity.RemoteAction;
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

    // offer | answer | candidate | call_history_request
    private String type;

    // SDP
    private String sdp;

    // ICE Candidate
    private String candidate;

    private String sdpMid;

    private Integer sdpMLineIndex;

    // Existing remote action
    private RemoteAction action;

    // New: Call History Request
    private CallHistoryRequest callHistoryRequest;

    private Integer screenWidth;

    private Integer screenHeight;

    private Integer rotation;
}