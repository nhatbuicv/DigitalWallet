package com.nhatbui.digital_wallet.repository;

import com.nhatbui.digital_wallet.entity.PasswordResetToken;
import com.nhatbui.digital_wallet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    List<PasswordResetToken> findByExpiresAtAfter(Instant now);

    void deleteByUserUid(User user);
}
