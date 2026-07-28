package com.manish.device_tracker_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_audio")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Audio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String audioName;

    @Column(nullable = false)
    private String audioUrl;

    @Column(nullable = false)
    private Long audioSize;

    // milliseconds
    private Long duration;

    private String bucketId;

    private String folderName;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}