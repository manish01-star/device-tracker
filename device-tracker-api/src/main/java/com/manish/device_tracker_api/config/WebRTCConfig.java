package com.manish.device_tracker_api.config;


import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;


@Configuration
public class WebRTCConfig {


    public List<Map<String,String>> getIceServers(){


        return List.of(

                Map.of(
                        "urls",
                        "stun:stun.l.google.com:19302"
                )

        );

    }


}