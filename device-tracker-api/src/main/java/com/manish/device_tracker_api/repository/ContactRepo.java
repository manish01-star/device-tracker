package com.manish.device_tracker_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.manish.device_tracker_api.entity.Contact;

import jakarta.transaction.Transactional;

public interface ContactRepo extends JpaRepository<Contact, Long> {

    @Transactional
    void deleteByDeviceId(String deviceId);

    List<Contact> findByDeviceIdOrderByContactNameAsc(String deviceId);

}
