package com.manish.device_tracker_api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.manish.device_tracker_api.entity.DeviceAudioFolder;

public interface DeviceAudioFolderRepo extends JpaRepository<DeviceAudioFolder, Long> {

    List<DeviceAudioFolder> findByDeviceIdOrderByFolderNameAsc(String deviceId);

    void deleteByDeviceId(String deviceId);

}
