package com.nhatbui.wallet_ledger.repository;

import com.nhatbui.wallet_ledger.entity.DeviceSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceSessionRepository extends JpaRepository<DeviceSession, String> {
    Optional<DeviceSession> findByDeviceId(String deviceId);
}
