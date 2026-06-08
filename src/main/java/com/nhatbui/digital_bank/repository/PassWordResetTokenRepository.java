package com.nhatbui.digital_bank.repository;

import com.nhatbui.digital_bank.entity.PasswordResetToken;
import com.nhatbui.digital_bank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PassWordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    List<PasswordResetToken> findByExpiresAtAfter(Instant now);

    void deleteByUserUid(User user);
}
