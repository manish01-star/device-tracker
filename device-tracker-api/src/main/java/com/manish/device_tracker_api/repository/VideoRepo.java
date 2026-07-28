package com.manish.device_tracker_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.manish.device_tracker_api.entity.Video;

import jakarta.transaction.Transactional;

public interface VideoRepo extends JpaRepository<Video, Long> {

    @Transactional
    void deleteByDeviceId(String deviceId);

    List<Video> findByDeviceIdOrderByCreatedAtDesc(String deviceId);

    List<Video> findByDeviceIdOrderByVideoNameAsc(String deviceId);

    Optional<Video> findByDeviceIdAndVideoNameAndVideoSize(
            String deviceId,
            String videoName,
            Long videoSize
    );

}