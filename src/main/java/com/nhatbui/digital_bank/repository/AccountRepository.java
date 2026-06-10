package com.nhatbui.digital_bank.repository;

import com.nhatbui.digital_bank.dto.response.BalanceResponse;
import com.nhatbui.digital_bank.entity.Account;
import com.nhatbui.digital_bank.enums.AccountStatus;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByAccountNumberAndUserUid(String accountNumber, UUID user_uid);

    List<Account> findAllByUserUid(UUID uid);

    long countByUserUidAndStatus(UUID user_uid , AccountStatus status);

    Optional<Account> findByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.uid = :uid")
    Optional<Account> findByUidWithLock(@Param("uid") UUID uid);
}
