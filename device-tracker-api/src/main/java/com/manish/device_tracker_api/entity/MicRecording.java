package com.manish.device_tracker_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mic_recording")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MicRecording {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceId;

    // Saved file name
    private String fileName;

    // /uploads/mic/abc123.mp3
    private String fileUrl;

    // Bytes
    private Long fileSize;

    // Seconds
    private Integer duration;

    private LocalDateTime createdAt;
}