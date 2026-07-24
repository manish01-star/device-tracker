package com.manish.device_tracker_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_image_folder")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceImageFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceId;

    private String bucketId;

    private String folderName;

    private Integer imageCount;

    private LocalDateTime syncedAt;
}
