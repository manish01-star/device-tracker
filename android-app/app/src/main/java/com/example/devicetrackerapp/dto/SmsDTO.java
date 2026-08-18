package com.example.devicetrackerapp.dto;

import com.google.gson.annotations.SerializedName;

public class SmsDTO {

    @SerializedName("smsId")
    private Long smsId;

    @SerializedName("deviceId")
    private String deviceId;

    @SerializedName("contactName")
    private String contactName;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("messageBody")
    private String messageBody;

    /**
     * RECEIVED / SENT / DRAFT / FAILED / OUTBOX / QUEUED
     */
    @SerializedName("smsType")
    private String smsType;

    /**
     * yyyy-MM-dd'T'HH:mm:ss
     */
    @SerializedName("smsDate")
    private String smsDate;

    @SerializedName("readStatus")
    private Boolean readStatus;

    @SerializedName("threadId")
    private String threadId;

    public SmsDTO() {
    }

    public Long getSmsId() {
        return smsId;
    }

    public void setSmsId(Long smsId) {
        this.smsId = smsId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getSmsType() {
        return smsType;
    }

    public void setSmsType(String smsType) {
        this.smsType = smsType;
    }

    public String getSmsDate() {
        return smsDate;
    }

    public void setSmsDate(String smsDate) {
        this.smsDate = smsDate;
    }

    public Boolean getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(Boolean readStatus) {
        this.readStatus = readStatus;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }
}