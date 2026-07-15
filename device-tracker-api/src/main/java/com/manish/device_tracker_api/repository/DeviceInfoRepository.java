package com.manish.device_tracker_api.repository;

import com.manish.device_tracker_api.entity.DeviceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceInfoRepository extends JpaRepository<DeviceInfo, Long> {

    Optional<DeviceInfo> findByDeviceId(String deviceId);

    boolean existsByDeviceId(String deviceId);
}