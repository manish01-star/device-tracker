package com.manish.device_tracker_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NominatimResponse {

    @JsonProperty("display_name")
    private String displayName;

}