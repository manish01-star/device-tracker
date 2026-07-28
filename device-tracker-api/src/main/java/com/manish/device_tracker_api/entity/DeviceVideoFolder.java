package com.manish.device_tracker_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_video_folder")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceVideoFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceId;

    private String bucketId;

    private String folderName;

    private Integer videoCount;

    private LocalDateTime syncedAt;

}