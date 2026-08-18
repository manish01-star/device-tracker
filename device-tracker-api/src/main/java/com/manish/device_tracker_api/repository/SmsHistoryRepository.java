package com.manish.device_tracker_api.repository;

import com.manish.device_tracker_api.entity.SmsHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SmsHistoryRepository
        extends JpaRepository<SmsHistory, Long> {

    /**
     * Get SMS history of a device within date range.
     */
    List<SmsHistory> findByDeviceIdAndSmsDateBetweenOrderBySmsDateDesc(
            String deviceId,
            LocalDateTime fromDate,
            LocalDateTime toDate
    );

    /**
     * Find SMS using Android's original SMS ID.
     *
     * Used for duplicate prevention.
     */
    Optional<SmsHistory> findByDeviceIdAndSmsId(
            String deviceId,
            Long smsId
    );

    /**
     * Count all SMS of a device.
     */
    long countByDeviceId(String deviceId);

    /**
     * Delete all SMS of a device.
     */
    void deleteByDeviceId(String deviceId);
}