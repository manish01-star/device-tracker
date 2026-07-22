package com.manish.device_tracker_api.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContactResponse {

    private Long id;

    private String contactName;

    private String phoneNumber;

    private LocalDateTime createdAt;

}