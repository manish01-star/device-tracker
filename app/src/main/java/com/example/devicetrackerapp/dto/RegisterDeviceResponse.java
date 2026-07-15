package com.example.devicetrackerapp.dto;

public class RegisterDeviceResponse {

    private Long id;
    private String message;

    public RegisterDeviceResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}