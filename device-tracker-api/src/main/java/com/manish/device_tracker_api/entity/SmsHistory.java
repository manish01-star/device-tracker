package com.manish.device_tracker_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "sms_history",

        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_sms_device_sms_id",
                        columnNames = {
                                "device_id",
                                "sms_id"
                        }
                )
        },

        indexes = {
                @Index(
                        name = "idx_sms_device_date",
                        columnList = "device_id,sms_date"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Device ID
     */
    @Column(
            name = "device_id",
            nullable = false
    )
    private String deviceId;

    /**
     * Original SMS ID from Android ContentResolver.
     *
     * Android SMS _id
     */
    @Column(
            name = "sms_id",
            nullable = false
    )
    private Long smsId;

    /**
     * Contact name
     */
    @Column(name = "contact_name")
    private String contactName;

    /**
     * Phone number
     */
    @Column(name = "phone_number")
    private String phoneNumber;

    /**
     * SMS message body
     */
    @Column(
            name = "message_body",
            columnDefinition = "TEXT"
    )
    private String messageBody;

    /**
     * RECEIVED / SENT / DRAFT / FAILED etc.
     */
    @Column(name = "sms_type")
    private String smsType;

    /**
     * Original SMS date/time from Android
     */
    @Column(
            name = "sms_date",
            nullable = false
    )
    private LocalDateTime smsDate;

    /**
     * Read / Unread
     */
    @Column(name = "read_status")
    private Boolean readStatus;

    /**
     * Android conversation/thread ID
     */
    @Column(name = "thread_id")
    private String threadId;

    /**
     * Backend DB record creation time
     */
    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}