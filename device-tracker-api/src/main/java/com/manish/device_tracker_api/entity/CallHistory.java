package com.manish.device_tracker_api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "call_history")
@Data
public class CallHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String phoneNumber;

    private String callType;

    @Column(nullable = false)
    private LocalDateTime callDate;

    private Long duration;

    private String contactName;
}