package com.example.devicetrackerapp.dto;

import java.time.LocalDate;
import java.util.List;

public class CallHistorySyncRequest {

    private String deviceId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<CallHistorySyncItemDTO> calls;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public List<CallHistorySyncItemDTO> getCalls() {
        return calls;
    }

    public void setCalls(List<CallHistorySyncItemDTO> calls) {
        this.calls = calls;
    }
}
