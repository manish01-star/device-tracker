package com.manish.device_tracker_api.repository;

import com.manish.device_tracker_api.entity.MicRecording;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MicRecordingRepo extends JpaRepository<MicRecording, Long> {

    List<MicRecording> findByDeviceIdOrderByCreatedAtDesc(String deviceId);

    @Transactional
    void deleteByDeviceId(String deviceId);

}