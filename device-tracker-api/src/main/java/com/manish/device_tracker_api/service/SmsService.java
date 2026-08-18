package com.manish.device_tracker_api.service;

import com.manish.device_tracker_api.dto.SmsDTO;
import com.manish.device_tracker_api.dto.SmsSyncPayload;
import com.manish.device_tracker_api.dto.SmsSyncRequestDTO;
import com.manish.device_tracker_api.entity.DeviceInfo;
import com.manish.device_tracker_api.entity.SmsHistory;
import com.manish.device_tracker_api.repository.DeviceInfoRepository;
import com.manish.device_tracker_api.repository.SmsHistoryRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SmsService {

    private final SmsHistoryRepository smsHistoryRepository;

    private final DeviceInfoRepository deviceRepository;

    /**
     * =========================================================
     * Dashboard -> Backend
     *
     * Request SMS history from device.
     * =========================================================
     */
    @Transactional
    public void requestSmsHistory(
            SmsSyncRequestDTO request) {

        validateRequest(request);

        LocalDate fromDate = LocalDate.parse(request.getFromDate());

        LocalDate toDate = LocalDate.parse(request.getToDate());

        if (fromDate.isAfter(toDate)) {

            throw new IllegalArgumentException(
                    "From date cannot be after To date");
        }

        DeviceInfo device = deviceRepository
                .findByDeviceId(
                        request.getDeviceId())
                .orElseThrow(
                        () -> new RuntimeException(
                                "Device not found"));

        /*
         * Tell Android that SMS sync is required.
         */
        device.setRefreshSms(true);

        /*
         * Previous upload status reset.
         */
        device.setSmsUploaded(false);

        /*
         * Selected date range.
         */
        device.setSmsFromDate(fromDate);

        device.setSmsToDate(toDate);

        deviceRepository.save(device);
    }

    /**
     * =========================================================
     * Android -> Backend
     *
     * Save SMS received from device.
     * =========================================================
     */
    @Transactional
    public int saveSmsFromDevice(
            SmsSyncPayload payload) {

        /*
         * Validate payload.
         */
        if (payload == null) {

            throw new IllegalArgumentException(
                    "SMS payload is required");
        }

        if (payload.getDeviceId() == null ||
                payload.getDeviceId().isBlank()) {

            throw new IllegalArgumentException(
                    "Device ID is required");
        }

        /*
         * Verify device exists.
         */
        DeviceInfo device = deviceRepository
                .findByDeviceId(
                        payload.getDeviceId())
                .orElseThrow(
                        () -> new RuntimeException(
                                "Device not found"));

        /*
         * If Android sends empty SMS list,
         * still mark sync as completed.
         */
        if (payload.getSmsList() == null ||
                payload.getSmsList().isEmpty()) {

            device.setSmsUploaded(true);

            device.setRefreshSms(false);

            deviceRepository.save(device);

            return 0;
        }

        int savedCount = 0;

        /*
         * Process every SMS.
         */
        for (SmsDTO dto : payload.getSmsList()) {

            /*
             * Ignore null item.
             */
            if (dto == null) {
                continue;
            }

            /*
             * SMS ID is required because it is used
             * to prevent duplicate SMS records.
             */
            if (dto.getSmsId() == null) {

                continue;
            }

            /*
             * SMS date is required for date-range
             * searching.
             */
            if (dto.getSmsDate() == null) {

                continue;
            }

            /*
             * Check duplicate.
             *
             * Same SMS ID can exist on different devices,
             * therefore deviceId + smsId is checked.
             */
            boolean alreadyExists = smsHistoryRepository.findByDeviceIdAndSmsId(
                    payload.getDeviceId(),
                    dto.getSmsId())
                    .isPresent();

            if (alreadyExists) {

                /*
                 * Same SMS already exists.
                 * Don't insert duplicate.
                 */
                continue;
            }

            /*
             * Create DB entity.
             */
            SmsHistory sms = SmsHistory.builder()

                    .deviceId(
                            payload.getDeviceId())

                    .smsId(
                            dto.getSmsId())

                    .contactName(
                            dto.getContactName())

                    .phoneNumber(
                            dto.getPhoneNumber())

                    .messageBody(
                            dto.getMessageBody())

                    .smsType(
                            dto.getSmsType())

                    .smsDate(
                            dto.getSmsDate())

                    .readStatus(
                            dto.getReadStatus())

                    .threadId(
                            dto.getThreadId())

                    .build();

            /*
             * Save SMS.
             */
            smsHistoryRepository.save(sms);

            savedCount++;
        }

        /*
         * Mark SMS sync completed.
         */
        device.setSmsUploaded(true);

        device.setRefreshSms(false);

        deviceRepository.save(device);

        return savedCount;
    }

    /**
     * =========================================================
     * Dashboard -> Backend
     *
     * Get SMS from DB according to date range.
     * =========================================================
     */
    public List<SmsDTO> getSmsHistory(
            SmsSyncRequestDTO request) {

        validateRequest(request);

        LocalDate fromDate = LocalDate.parse(
                request.getFromDate());

        LocalDate toDate = LocalDate.parse(
                request.getToDate());

        if (fromDate.isAfter(toDate)) {

            throw new IllegalArgumentException(
                    "From date cannot be after To date");
        }

        /*
         * Start of From Date.
         *
         * Example:
         * 2026-08-01 -> 2026-08-01 00:00:00
         */
        LocalDateTime from = fromDate.atStartOfDay();

        /*
         * End of To Date.
         *
         * Example:
         * 2026-08-05 -> 2026-08-05 23:59:59.999...
         */
        LocalDateTime to = toDate.atTime(
                LocalTime.MAX);

        return smsHistoryRepository
                .findByDeviceIdAndSmsDateBetweenOrderBySmsDateDesc(
                        request.getDeviceId(),
                        from,
                        to)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * =========================================================
     * Entity -> DTO
     * =========================================================
     */
    private SmsDTO mapToDTO(
            SmsHistory sms) {

        return SmsDTO.builder()

                .id(
                        sms.getId())

                .smsId(
                        sms.getSmsId())

                .deviceId(
                        sms.getDeviceId())

                .contactName(
                        sms.getContactName())

                .phoneNumber(
                        sms.getPhoneNumber())

                .messageBody(
                        sms.getMessageBody())

                .smsType(
                        sms.getSmsType())

                .smsDate(
                        sms.getSmsDate())

                .readStatus(
                        sms.getReadStatus())

                .threadId(
                        sms.getThreadId())

                .build();
    }

    /**
     * =========================================================
     * Delete SMS
     * =========================================================
     */
    @Transactional
    public void deleteSms(
            String deviceId) {

        if (deviceId == null ||
                deviceId.isBlank()) {

            throw new IllegalArgumentException(
                    "Device ID is required");
        }

        /*
         * Verify device exists.
         */
        deviceRepository
                .findByDeviceId(deviceId)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Device not found"));

        smsHistoryRepository
                .deleteByDeviceId(deviceId);
    }

    /**
     * =========================================================
     * Common request validation
     * =========================================================
     */
    private void validateRequest(
            SmsSyncRequestDTO request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "SMS request is required");
        }

        if (request.getDeviceId() == null ||
                request.getDeviceId().isBlank()) {

            throw new IllegalArgumentException(
                    "Device ID is required");
        }

        if (request.getFromDate() == null ||
                request.getFromDate().isBlank()) {

            throw new IllegalArgumentException(
                    "From date is required");
        }

        if (request.getToDate() == null ||
                request.getToDate().isBlank()) {

            throw new IllegalArgumentException(
                    "To date is required");
        }
    }
}