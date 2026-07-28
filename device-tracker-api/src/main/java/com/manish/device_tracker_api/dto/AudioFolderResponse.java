package com.manish.device_tracker_api.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioFolderResponse {

    private String bucketId;

    private String folderName;

    private Integer audioCount;

}