package com.example.devicetrackerapp.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SmsSyncPayload {

    /**
     * Device ID
     */
    @SerializedName("deviceId")
    private String deviceId;

    /**
     * SMS list
     */
    @SerializedName("smsList")
    private List<SmsDTO> smsList;

    public SmsSyncPayload() {
    }

    public SmsSyncPayload(
            String deviceId,
            List<SmsDTO> smsList) {

        this.deviceId = deviceId;
        this.smsList = smsList;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<SmsDTO> getSmsList() {
        return smsList;
    }

    public void setSmsList(List<SmsDTO> smsList) {
        this.smsList = smsList;
    }
}