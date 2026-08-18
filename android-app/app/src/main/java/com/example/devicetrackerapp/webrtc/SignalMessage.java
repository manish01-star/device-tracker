package com.example.devicetrackerapp.webrtc;

import com.example.devicetrackerapp.model.RemoteAction;

import com.example.devicetrackerapp.dto.CallHistoryRequest;
public class SignalMessage {

    private String type;

    private String deviceId;

    private String sdp;

    private String candidate;

    private String sdpMid;

    private Integer sdpMLineIndex;

    private Integer screenWidth;

    private Integer screenHeight;

    private Integer rotation;


    // NEW
    private RemoteAction action;

    private CallHistoryRequest callHistoryRequest;

    public SignalMessage() {
    }

    public CallHistoryRequest getCallHistoryRequest() {
        return callHistoryRequest;
    }

    public void setCallHistoryRequest(
            CallHistoryRequest callHistoryRequest
    ) {
        this.callHistoryRequest = callHistoryRequest;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }


    public String getSdp() {
        return sdp;
    }

    public void setSdp(String sdp) {
        this.sdp = sdp;
    }


    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }


    public String getSdpMid() {
        return sdpMid;
    }

    public void setSdpMid(String sdpMid) {
        this.sdpMid = sdpMid;
    }


    public Integer getSdpMLineIndex() {
        return sdpMLineIndex;
    }

    public void setSdpMLineIndex(Integer sdpMLineIndex) {
        this.sdpMLineIndex = sdpMLineIndex;
    }


    // NEW

    public RemoteAction getAction() {
        return action;
    }


    public void setAction(RemoteAction action) {
        this.action = action;
    }

    public Integer getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(Integer screenWidth) {
        this.screenWidth = screenWidth;
    }

    public Integer getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(Integer screenHeight) {
        this.screenHeight = screenHeight;
    }

    public Integer getRotation() {
        return rotation;
    }

    public void setRotation(Integer rotation) {
        this.rotation = rotation;
    }

}