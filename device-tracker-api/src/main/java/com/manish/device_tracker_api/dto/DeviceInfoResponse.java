package com.manish.device_tracker_api.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceInfoResponse {

    private Long id;
    private String username;
    private String deviceId;
    private String deviceModel;
    private String appVersion;
    private Double latitude;
    private Double longitude;
    private String address;
    private Integer battery;
    private LocalDateTime lastOnline;

    private Boolean trackingEnabled;
    private Integer trackingInterval;

    private Boolean contactsUploaded;
    private Boolean refreshContacts;

    private Boolean imagesUploaded;
    private Boolean refreshImages;

    private Boolean refreshVideos;
    private Boolean videosUploaded;

    private Boolean audiosUploaded;
    private Boolean refreshAudios;

    private Boolean refreshMic;
    private Integer micDuration;
    private Boolean micUploaded;

    private Boolean refreshCamera;
    private Boolean cameraStreaming;
    private String cameraType;

    private Boolean refreshCallHistory;
}