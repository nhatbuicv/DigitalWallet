package com.nhatbui.digital_wallet.repository;

import com.nhatbui.digital_wallet.entity.Wallet;
import com.nhatbui.digital_wallet.enums.WalletStatus;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findByAccountNumberAndUserUid(String accountNumber, UUID user_uid);

    List<Wallet> findAllByUserUid(UUID uid);

    long countByUserUidAndStatus(UUID user_uid , WalletStatus status);

    Optional<Wallet> findByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Wallet a WHERE a.uid = :uid")
    Optional<Wallet> findByUidWithLock(@Param("uid") UUID uid);
}
