package com.manish.device_tracker_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.manish.device_tracker_api.entity.Video;
import jakarta.transaction.Transactional;

public interface VideoRepo extends JpaRepository<Video, Long> {

    @Transactional
    void deleteByDeviceId(String deviceId);

    List<Video> findByDeviceIdOrderByCreatedAtDesc(String deviceId);
}
