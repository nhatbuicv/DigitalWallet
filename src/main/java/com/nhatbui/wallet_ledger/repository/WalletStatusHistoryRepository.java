package com.nhatbui.wallet_ledger.repository;

import com.nhatbui.wallet_ledger.entity.WalletStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WalletStatusHistoryRepository extends JpaRepository<WalletStatusHistory, UUID> {
}