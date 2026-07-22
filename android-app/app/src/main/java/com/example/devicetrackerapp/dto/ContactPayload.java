package com.example.devicetrackerapp.dto;

import java.util.List;

public class ContactPayload {

    private String deviceId;
    private List<ContactItem> contacts;

    public ContactPayload() {
    }

    public ContactPayload(String deviceId, List<ContactItem> contacts) {
        this.deviceId = deviceId;
        this.contacts = contacts;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<ContactItem> getContacts() {
        return contacts;
    }

    public void setContacts(List<ContactItem> contacts) {
        this.contacts = contacts;
    }
}