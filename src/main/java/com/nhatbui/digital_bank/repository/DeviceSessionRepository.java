package com.nhatbui.digital_bank.repository;

import com.nhatbui.digital_bank.entity.DeviceSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceSessionRepository extends JpaRepository<DeviceSession, String> {
    Optional<DeviceSession> findByDeviceId(String deviceId);
}
