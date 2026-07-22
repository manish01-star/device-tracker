package com.manish.device_tracker_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageItem {

    private String imageName;
    private String imagePath;
    private Long imageSize;

}
