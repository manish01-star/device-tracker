package com.manish.device_tracker_api.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.manish.device_tracker_api.entity.Audio;
import com.manish.device_tracker_api.entity.DeviceAudioFolder;

public interface AudioRepo extends JpaRepository<Audio, Long> {

    Optional<Audio> findByDeviceIdAndAudioNameAndAudioSize(
            String deviceId,
            String audioName,
            Long audioSize);

    List<Audio> findByDeviceIdOrderByCreatedAtDesc(String deviceId);

    void deleteByDeviceId(String deviceId);

    List<DeviceAudioFolder> findByDeviceIdOrderByFolderNameAsc(String deviceId);

}