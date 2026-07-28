package com.manish.device_tracker_api.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MicRecordingResponse {

    private Long id;

    private String fileName;

    private String fileUrl;

    private Long fileSize;

    private Integer duration;

    private LocalDateTime createdAt;

}