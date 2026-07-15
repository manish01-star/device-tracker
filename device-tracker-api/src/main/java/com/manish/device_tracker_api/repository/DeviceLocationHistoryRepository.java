package com.manish.device_tracker_api.repository;

import com.manish.device_tracker_api.entity.DeviceLocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeviceLocationHistoryRepository extends JpaRepository<DeviceLocationHistory, Long> {

    List<DeviceLocationHistory> findByDeviceIdOrderByCreatedAtAsc(String deviceId);

}