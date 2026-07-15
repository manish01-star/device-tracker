package com.manish.device_tracker_api.service;

import com.manish.device_tracker_api.dto.DeviceInfoResponse;
import com.manish.device_tracker_api.dto.RegisterDeviceRequest;
import com.manish.device_tracker_api.dto.RegisterDeviceResponse;
import com.manish.device_tracker_api.dto.TrackingConfigResponse;
import com.manish.device_tracker_api.dto.UpdateDeviceRequest;
import com.manish.device_tracker_api.dto.UpdateTrackingRequest;
import com.manish.device_tracker_api.entity.DeviceInfo;
import com.manish.device_tracker_api.repository.DeviceInfoRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceInfoService {

        private final DeviceInfoRepository deviceInfoRepository;
        private final GeoCodingService geoCodingService;

        public RegisterDeviceResponse registerDevice(RegisterDeviceRequest request) {

                DeviceInfo deviceInfo = deviceInfoRepository
                                .findByDeviceId(request.getDeviceId())
                                .orElse(new DeviceInfo());

                deviceInfo.setUsername(request.getUsername());
                deviceInfo.setDeviceId(request.getDeviceId());
                deviceInfo.setDeviceModel(request.getDeviceModel());
                deviceInfo.setAppVersion(request.getAppVersion());

                DeviceInfo savedDevice = deviceInfoRepository.save(deviceInfo);

                return RegisterDeviceResponse.builder()
                                .id(savedDevice.getId())
                                .username(savedDevice.getUsername())
                                .deviceId(savedDevice.getDeviceId())
                                .deviceModel(savedDevice.getDeviceModel())
                                .appVersion(savedDevice.getAppVersion())
                                .build();
        }

        public void updateDevice(UpdateDeviceRequest request) {

                DeviceInfo deviceInfo = deviceInfoRepository.findByDeviceId(request.getDeviceId())
                                .orElseThrow(() -> new RuntimeException("Device not found"));

                // Location Update
                if (request.getLatitude() != null && request.getLongitude() != null) {

                        Double oldLat = deviceInfo.getLatitude();
                        Double oldLng = deviceInfo.getLongitude();

                        deviceInfo.setLatitude(request.getLatitude());
                        deviceInfo.setLongitude(request.getLongitude());

                        boolean locationChanged = false;

                        if (oldLat == null || oldLng == null) {

                                locationChanged = true;

                        } else {

                                double latDiff = Math.abs(oldLat - request.getLatitude());
                                double lngDiff = Math.abs(oldLng - request.getLongitude());

                                if (latDiff > 0.0005 || lngDiff > 0.0005) {
                                        locationChanged = true;
                                }
                        }

                        if (locationChanged) {

                                String address = geoCodingService.getAddress(
                                                request.getLatitude(),
                                                request.getLongitude());

                                if (address != null && !address.isBlank()) {
                                        deviceInfo.setAddress(address);
                                }
                        }
                }

                // Battery
                if (request.getBattery() != null) {
                        deviceInfo.setBattery(request.getBattery());
                }

                // Last Online
                deviceInfo.setLastOnline(LocalDateTime.now());

                deviceInfoRepository.save(deviceInfo);
        }

        public DeviceInfoResponse getDevice(String deviceId) {

                DeviceInfo device = deviceInfoRepository.findByDeviceId(deviceId)
                                .orElseThrow(() -> new RuntimeException("Device not found"));

                return DeviceInfoResponse.builder()
                                .id(device.getId())
                                .username(device.getUsername())
                                .deviceId(device.getDeviceId())
                                .deviceModel(device.getDeviceModel())
                                .appVersion(device.getAppVersion())
                                .latitude(device.getLatitude())
                                .longitude(device.getLongitude())
                                .address(device.getAddress())
                                .battery(device.getBattery())
                                .trackingEnabled(device.getTrackingEnabled())
                                .trackingInterval(device.getTrackingInterval())
                                .lastOnline(device.getLastOnline())
                                .build();
        }

        public List<DeviceInfoResponse> getAllDevices() {

                List<DeviceInfo> devices = deviceInfoRepository.findAll();

                return devices.stream()
                                .map(device -> DeviceInfoResponse.builder()
                                                .id(device.getId())
                                                .username(device.getUsername())
                                                .deviceId(device.getDeviceId())
                                                .deviceModel(device.getDeviceModel())
                                                .appVersion(device.getAppVersion())
                                                .latitude(device.getLatitude())
                                                .longitude(device.getLongitude())
                                                .address(device.getAddress())
                                                .battery(device.getBattery())
                                                .trackingEnabled(device.getTrackingEnabled())
                                                .trackingInterval(device.getTrackingInterval())
                                                .lastOnline(device.getLastOnline())
                                                .build())
                                .toList();
        }

        public void deleteDevice(String deviceId) {

                DeviceInfo device = deviceInfoRepository.findByDeviceId(deviceId)
                                .orElseThrow(() -> new RuntimeException("Device not found."));

                deviceInfoRepository.delete(device);
        }

        public void updateTracking(UpdateTrackingRequest request) {

                DeviceInfo device = deviceInfoRepository
                                .findByDeviceId(request.getDeviceId())
                                .orElseThrow(
                                                () -> new RuntimeException("Device not found"));

                if (request.getTrackingEnabled() != null) {

                        device.setTrackingEnabled(
                                        request.getTrackingEnabled());

                }

                if (request.getTrackingInterval() != null) {

                        device.setTrackingInterval(
                                        request.getTrackingInterval());

                }

                deviceInfoRepository.save(device);

        }

        public TrackingConfigResponse getTrackingConfig(String deviceId) {

                DeviceInfo device = deviceInfoRepository.findByDeviceId(deviceId)
                                .orElseThrow(() -> new RuntimeException("Device not found"));

                return TrackingConfigResponse.builder()

                                .trackingEnabled(
                                                device.getTrackingEnabled())

                                .trackingInterval(
                                                device.getTrackingInterval())

                                .build();

        }
}