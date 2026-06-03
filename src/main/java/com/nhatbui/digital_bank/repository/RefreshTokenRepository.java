package com.nhatbui.digital_bank.repository;

import com.nhatbui.digital_bank.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    void deleteByUserUid(UUID userUid);

    void deleteByUserUidAndDeviceId(UUID uid, String deviceId);
}
