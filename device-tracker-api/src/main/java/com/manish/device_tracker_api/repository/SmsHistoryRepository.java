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
                        LocalDateTime toDate);

        /**
         * Find a specific Android SMS by device + original SMS ID.
         *
         * Used to prevent duplicate SMS records.
         */
        Optional<SmsHistory> findByDeviceIdAndSmsId(
                        String deviceId,
                        Long smsId);

        long countByDeviceId(String deviceId);

        void deleteByDeviceId(String deviceId);
}