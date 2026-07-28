package com.manish.device_tracker_api.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AudioFolderPayload {

    private String bucketId;

    private String folderName;

    private Integer audioCount;

}