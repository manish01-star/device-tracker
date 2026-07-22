package com.manish.device_tracker_api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.manish.device_tracker_api.entity.Audio;
import jakarta.transaction.Transactional;

public interface AudioRepo extends JpaRepository<Audio, Long> {

    @Transactional
    void deleteByDeviceId(String deviceId);

    List<Audio> findByDeviceIdOrderByCreatedAtDesc(String deviceId);

}