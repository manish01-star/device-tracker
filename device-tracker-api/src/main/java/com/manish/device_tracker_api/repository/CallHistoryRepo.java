package com.manish.device_tracker_api.repository;

import com.manish.device_tracker_api.entity.CallHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CallHistoryRepo extends JpaRepository<CallHistory, Long> {

        List<CallHistory> findByDeviceIdAndCallDateBetweenOrderByCallDateDesc(
                        String deviceId,
                        LocalDateTime fromDate,
                        LocalDateTime toDate);

        boolean existsByDeviceIdAndPhoneNumberAndCallTypeAndCallDateAndDuration(
                        String deviceId,
                        String phoneNumber,
                        String callType,
                        LocalDateTime callDate,
                        Long duration);
}
