package com.manish.device_tracker_api.service;

import com.manish.device_tracker_api.dto.NominatimResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class GeoCodingService {

    private final RestTemplate restTemplate;

    public GeoCodingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getAddress(double latitude, double longitude) {

        try {

            String url = "https://nominatim.openstreetmap.org/reverse" +
                    "?format=json" +
                    "&lat=" + latitude +
                    "&lon=" + longitude +
                    "&zoom=18" +
                    "&addressdetails=1";

            HttpHeaders headers = new HttpHeaders();

            headers.set(
                    HttpHeaders.USER_AGENT,
                    "DeviceTracker/1.0");

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<NominatimResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    NominatimResponse.class);

            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null) {

                return response
                        .getBody()
                        .getDisplayName();

            }

        } catch (Exception e) {

            log.error("Reverse Geocoding Failed", e);

        }

        return null;

    }

}