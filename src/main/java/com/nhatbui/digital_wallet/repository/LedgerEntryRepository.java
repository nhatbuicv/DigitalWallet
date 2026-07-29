package com.nhatbui.digital_wallet.repository;

import com.nhatbui.digital_wallet.entity.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID>  , JpaSpecificationExecutor<LedgerEntry> {
    Page<LedgerEntry> findByWalletUid(UUID walletId, Pageable pageable);
}
