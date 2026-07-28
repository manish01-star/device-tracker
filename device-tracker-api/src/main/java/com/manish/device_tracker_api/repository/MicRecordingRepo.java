package com.manish.device_tracker_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.manish.device_tracker_api.entity.MicRecording;

public interface MicRecordingRepo extends JpaRepository<MicRecording, Long> {

    List<MicRecording> findByDeviceIdOrderByCreatedAtDesc(String deviceId);

    @Transactional
    void deleteByDeviceId(String deviceId);

}