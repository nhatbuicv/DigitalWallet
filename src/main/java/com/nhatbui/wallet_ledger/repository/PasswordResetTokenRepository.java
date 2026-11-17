package com.nhatbui.wallet_ledger.repository;

import com.nhatbui.wallet_ledger.entity.PasswordResetToken;
import com.nhatbui.wallet_ledger.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    List<PasswordResetToken> findByExpiresAtAfter(Instant now);

    void deleteByUserUid(User user);
}
