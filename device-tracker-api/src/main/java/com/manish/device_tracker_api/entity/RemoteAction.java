package com.manish.device_tracker_api.entity;

import lombok.Data;

@Data
public class RemoteAction {

    private String deviceId;

    private String type;

    private Float x;

    private Float y;

    private Float endX;

    private Float endY;

    private Long duration;

}