package com.manish.device_tracker_api.controller;

import com.manish.device_tracker_api.dto.SmsDTO;
import com.manish.device_tracker_api.dto.SmsSyncPayload;
import com.manish.device_tracker_api.dto.SmsSyncRequestDTO;
import com.manish.device_tracker_api.service.SmsService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/media/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;


    /**
     * =========================================================
     * Dashboard -> Backend
     *
     * Request SMS history from device.
     *
     * POST /media/sms/request
     * =========================================================
     */
    @PostMapping("/request")
    public ResponseEntity<?> requestSmsHistory(
            @RequestBody SmsSyncRequestDTO request) {

        try {

            smsService.requestSmsHistory(request);

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    true
            );

            response.put(
                    "message",
                    "SMS history request sent to device"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    false
            );

            response.put(
                    "message",
                    e.getMessage()
            );

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }
    }


    /**
     * =========================================================
     * Android -> Backend
     *
     * Sync SMS into database.
     *
     * POST /media/sms/sync
     * =========================================================
     */
    @PostMapping("/sync")
    public ResponseEntity<?> syncSms(
            @RequestBody SmsSyncPayload payload) {

        try {

            int count =
                    smsService.saveSmsFromDevice(
                            payload
                    );

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    true
            );

            response.put(
                    "message",
                    "SMS synced successfully"
            );

            response.put(
                    "savedCount",
                    count
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    false
            );

            response.put(
                    "message",
                    e.getMessage()
            );

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }
    }


    /**
     * =========================================================
     * Dashboard -> Backend
     *
     * Get SMS from database.
     *
     * POST /media/sms
     * =========================================================
     */
    @PostMapping
    public ResponseEntity<?> getSmsHistory(
            @RequestBody SmsSyncRequestDTO request) {

        try {

            List<SmsDTO> data =
                    smsService.getSmsHistory(
                            request
                    );

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    true
            );

            response.put(
                    "data",
                    data
            );

            response.put(
                    "totalRecords",
                    data.size()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    false
            );

            response.put(
                    "message",
                    e.getMessage()
            );

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }
    }


    /**
     * =========================================================
     * Delete all SMS of device.
     *
     * DELETE /media/sms/{deviceId}
     * =========================================================
     */
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<?> deleteSms(
            @PathVariable String deviceId) {

        try {

            smsService.deleteSms(
                    deviceId
            );

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    true
            );

            response.put(
                    "message",
                    "SMS history deleted successfully"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    false
            );

            response.put(
                    "message",
                    e.getMessage()
            );

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }
    }
}