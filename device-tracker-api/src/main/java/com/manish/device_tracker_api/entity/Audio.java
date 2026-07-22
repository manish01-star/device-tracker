package com.manish.device_tracker_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Audio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceId;

    private String audioName;

    private String audioUrl;

    private Long audioSize;

    private Long duration;

    private LocalDateTime createdAt;

}