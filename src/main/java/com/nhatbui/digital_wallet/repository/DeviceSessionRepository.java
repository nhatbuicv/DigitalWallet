package com.nhatbui.digital_wallet.repository;

import com.nhatbui.digital_wallet.entity.DeviceSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceSessionRepository extends JpaRepository<DeviceSession, String> {
    Optional<DeviceSession> findByDeviceId(String deviceId);
}
