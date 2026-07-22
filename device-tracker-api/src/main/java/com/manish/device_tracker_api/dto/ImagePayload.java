package com.manish.device_tracker_api.dto;

import java.util.List;
import lombok.Data;

@Data
public class ImagePayload {

    private String deviceId;
    private List<ImageItem> images;

}
