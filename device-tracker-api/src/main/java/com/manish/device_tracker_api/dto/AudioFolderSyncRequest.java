package com.manish.device_tracker_api.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AudioFolderSyncRequest {

    private String deviceId;

    private List<AudioFolderPayload> folders;

}