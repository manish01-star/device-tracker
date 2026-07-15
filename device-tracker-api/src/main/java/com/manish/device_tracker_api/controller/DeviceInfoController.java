package com.manish.device_tracker_api.controller;

import com.manish.device_tracker_api.dto.ApiResponse;
import com.manish.device_tracker_api.dto.DeviceInfoResponse;
import com.manish.device_tracker_api.dto.DeviceLocationHistoryResponse;
import com.manish.device_tracker_api.dto.RegisterDeviceRequest;
import com.manish.device_tracker_api.dto.RegisterDeviceResponse;
import com.manish.device_tracker_api.dto.TrackingConfigResponse;
import com.manish.device_tracker_api.dto.UpdateDeviceRequest;
import com.manish.device_tracker_api.dto.UpdateTrackingRequest;
import com.manish.device_tracker_api.service.DeviceInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
public class DeviceInfoController {

        private final DeviceInfoService deviceInfoService;

        @PostMapping("/register")
        public ApiResponse<RegisterDeviceResponse> registerDevice(
                        @Valid @RequestBody RegisterDeviceRequest request) {

                RegisterDeviceResponse response = deviceInfoService.registerDevice(request);

                return ApiResponse.<RegisterDeviceResponse>builder()
                                .success(true)
                                .message("Device registered successfully.")
                                .data(response)
                                .build();
        }

        @PostMapping("/update")
        public ApiResponse<String> updateDevice(
                        @Valid @RequestBody UpdateDeviceRequest request) {

                deviceInfoService.updateDevice(request);

                return ApiResponse.<String>builder()
                                .success(true)
                                .message("Device updated successfully.")
                                .data(null)
                                .build();
        }

        @GetMapping("/{deviceId}")
        public ApiResponse<DeviceInfoResponse> getDevice(
                        @PathVariable String deviceId) {

                DeviceInfoResponse response = deviceInfoService.getDevice(deviceId);

                return ApiResponse.<DeviceInfoResponse>builder()
                                .success(true)
                                .message("Device fetched successfully.")
                                .data(response)
                                .build();
        }

        @GetMapping("/all")
        public ApiResponse<List<DeviceInfoResponse>> getAllDevices() {

                List<DeviceInfoResponse> response = deviceInfoService.getAllDevices();

                return ApiResponse.<List<DeviceInfoResponse>>builder()
                                .success(true)
                                .message("Devices fetched successfully.")
                                .data(response)
                                .build();
        }

        @DeleteMapping("/{deviceId}")
        public ApiResponse<String> deleteDevice(@PathVariable String deviceId) {

                deviceInfoService.deleteDevice(deviceId);

                return ApiResponse.<String>builder()
                                .success(true)
                                .message("Device deleted successfully.")
                                .data(null)
                                .build();
        }

        @PostMapping("/tracking")
        public ApiResponse<String> updateTracking(
                        @Valid @RequestBody UpdateTrackingRequest request) {

                deviceInfoService.updateTracking(request);

                return ApiResponse.<String>builder()
                                .success(true)
                                .message("Tracking updated successfully.")
                                .data(null)
                                .build();
        }

        @GetMapping("/tracking/config/{deviceId}")
        public ApiResponse<TrackingConfigResponse> getTrackingConfig(
                        @PathVariable String deviceId) {

                return ApiResponse.<TrackingConfigResponse>builder()
                                .success(true)
                                .message("Success")
                                .data(deviceInfoService.getTrackingConfig(deviceId))
                                .build();
        }
}