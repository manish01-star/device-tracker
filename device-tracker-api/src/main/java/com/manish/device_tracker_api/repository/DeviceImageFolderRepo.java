package com.manish.device_tracker_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.manish.device_tracker_api.entity.DeviceImageFolder;

@Repository
public interface DeviceImageFolderRepo extends JpaRepository<DeviceImageFolder, Long> {

    List<DeviceImageFolder> findByDeviceIdOrderByFolderNameAsc(String deviceId);

    void deleteByDeviceId(String deviceId);
}
