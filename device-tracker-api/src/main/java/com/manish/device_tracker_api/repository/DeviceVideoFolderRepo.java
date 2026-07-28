package com.manish.device_tracker_api.repository;

import com.manish.device_tracker_api.entity.DeviceVideoFolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceVideoFolderRepo extends JpaRepository<DeviceVideoFolder, Long> {

    List<DeviceVideoFolder> findByDeviceIdOrderByFolderNameAsc(String deviceId);

    void deleteByDeviceId(String deviceId);

}