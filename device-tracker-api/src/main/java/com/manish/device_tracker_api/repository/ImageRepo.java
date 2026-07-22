package com.manish.device_tracker_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.manish.device_tracker_api.entity.Image;
import jakarta.transaction.Transactional;

public interface ImageRepo extends JpaRepository<Image, Long> {

    @Transactional
    void deleteByDeviceId(String deviceId);

    List<Image> findByDeviceIdOrderByImageNameAsc(String deviceId);

}
