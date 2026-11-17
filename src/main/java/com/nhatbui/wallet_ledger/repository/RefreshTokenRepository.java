package com.nhatbui.wallet_ledger.repository;

import com.nhatbui.wallet_ledger.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    void deleteByUserUid(UUID userUid);

    void deleteByUserUidAndDeviceId(UUID uid, String deviceId);
}
