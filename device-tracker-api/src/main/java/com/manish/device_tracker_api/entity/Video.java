package com.manish.device_tracker_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_video")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceId;

    private String videoName;

    // actual uploaded file
    private String videoUrl;

    private Long videoSize;

    private Long duration;

    private LocalDateTime createdAt;
}
